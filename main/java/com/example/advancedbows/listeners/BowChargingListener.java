package com.example.advancedbows.listeners;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.bows.SpecialBow;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.security.SecureRandom;
import java.util.*;
public class BowChargingListener implements Listener {
    private final AdvancedBows plugin;
    private final Map<UUID, Long> chargingStartTimes = new HashMap<>();
    private final Map<UUID, BukkitTask> chargingTasks = new HashMap<>();
    private final Map<UUID, Integer> visibleRings = new HashMap<>();
    private final Map<UUID, Integer> particlePositions = new HashMap<>();
    private final Map<UUID, Boolean> fullyChargedStatus = new HashMap<>();  
    private final Map<UUID, Double> chargeTimers = new HashMap<>();         
    private final Map<UUID, String> chargingBowTypes = new HashMap<>();     
    private static final int RING_TOP = 0;
    private static final int RING_RIGHT = 1;
    private static final int RING_BOTTOM = 2;
    private static final int RING_LEFT = 3;
    private static final int RING_COUNT = 4;
    private static final SecureRandom random = new SecureRandom();
    private static final long RING_APPEAR_INTERVAL = 300; 
    public BowChargingListener(AdvancedBows plugin) {
        this.plugin = plugin;
    }
    public int getVisibleRingCount(UUID playerId) {
        return visibleRings.getOrDefault(playerId, 0);
    }
    public boolean isFullyCharged(UUID playerId) {
        return fullyChargedStatus.getOrDefault(playerId, false);
    }
    @EventHandler
    public void onPlayerDrawBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        Optional<SpecialBow> specialBow = plugin.getBowManager().getBowForItem(item);
        if (!specialBow.isPresent()) return;
        String bowType = specialBow.get().getType();
        if (!bowType.equals("YOIMIYA") && !bowType.equals("EXPLOSION") && !bowType.equals("SOUL")) return;
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            if (!chargingStartTimes.containsKey(player.getUniqueId())) {
                plugin.getLogger().info("Player " + player.getName() + " started charging " + bowType + " bow");
                startCharging(player, bowType);
            }
        }
    }
    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        if (bow == null) return;
        Optional<SpecialBow> specialBow = plugin.getBowManager().getBowForItem(bow);
        if (!specialBow.isPresent()) return;
        String bowType = specialBow.get().getType();
        if (!bowType.equals("YOIMIYA") && !bowType.equals("EXPLOSION")) return;
        UUID playerId = player.getUniqueId();
        if (chargingStartTimes.containsKey(playerId)) {
            plugin.getLogger().info("Player " + player.getName() + " shot " + bowType + " bow");
            if (bowType.equals("YOIMIYA")) {
                long chargeTime = System.currentTimeMillis() - chargingStartTimes.getOrDefault(playerId, System.currentTimeMillis());
                int finalRingCount = (int) Math.min(RING_COUNT, chargeTime / RING_APPEAR_INTERVAL);
                visibleRings.put(playerId, finalRingCount);
            }
            boolean isCharged = fullyChargedStatus.getOrDefault(playerId, false);
            stopCharging(player, false);
            new BukkitRunnable() {
                @Override
                public void run() {
                    visibleRings.remove(playerId);
                    fullyChargedStatus.remove(playerId);
                }
            }.runTaskLater(plugin, 5L);
        }
    }
    @EventHandler
    public void onPlayerChangeItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (chargingStartTimes.containsKey(playerId)) {
            plugin.getLogger().info("Player " + player.getName() + " changed held item, stopping charging");
            stopCharging(player, true);
        }
    }
    @EventHandler
    public void onPlayerInteractCancel(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!chargingStartTimes.containsKey(playerId)) return;
        if (event.getAction() == org.bukkit.event.block.Action.PHYSICAL ||
                !event.getAction().name().contains("RIGHT_CLICK")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            Optional<SpecialBow> specialBow = plugin.getBowManager().getBowForItem(item);
            if (!specialBow.isPresent()) return;
            String bowType = specialBow.get().getType();
            if (!bowType.equals("YOIMIYA") && !bowType.equals("EXPLOSION")) return;
            if (!player.isHandRaised()) {
                plugin.getLogger().info("Player " + player.getName() + " cancelled bow charge");
                stopCharging(player, true);
            }
        }
    }
    private void startCharging(Player player, String bowType) {
        UUID playerId = player.getUniqueId();
        chargingStartTimes.put(playerId, System.currentTimeMillis());
        visibleRings.put(playerId, 0);
        particlePositions.put(playerId, 0);
        fullyChargedStatus.put(playerId, false);
        chargeTimers.put(playerId, 0.0);
        chargingBowTypes.put(playerId, bowType);
        double chargeTime = 3.0; 
        if (bowType.equals("EXPLOSION")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            Optional<SpecialBow> specialBow = plugin.getBowManager().getBowForItem(item);
            if (specialBow.isPresent()) {
                Map<String, Object> parameters = specialBow.get().getParameters(item);
                Object value = parameters.get("chargeTime");
                if (value instanceof Number) {
                    chargeTime = ((Number) value).doubleValue();
                }
            }
        }
        final double chargeTargetMs = chargeTime * 1000.0;
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopCharging(player, true);
                    return;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                Optional<SpecialBow> specialBow = plugin.getBowManager().getBowForItem(item);
                if (!specialBow.isPresent() || !specialBow.get().getType().equals(bowType)) {
                    stopCharging(player, true);
                    return;
                }
                if (!player.isHandRaised()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (chargingStartTimes.containsKey(playerId)) {
                                stopCharging(player, true);
                            }
                        }
                    }.runTaskLater(plugin, 5L);
                    return;
                }
                long chargeTime = System.currentTimeMillis() - chargingStartTimes.getOrDefault(playerId, System.currentTimeMillis());
                if (bowType.equals("YOIMIYA")) {
                    updateYoimiyaRings(player, chargeTime);
                } else if (bowType.equals("EXPLOSION")) {
                    double chargePercent = Math.min(1.0, chargeTime / chargeTargetMs);
                    chargeTimers.put(playerId, chargePercent);
                    boolean wasFullyCharged = fullyChargedStatus.getOrDefault(playerId, false);
                    boolean isNowFullyCharged = chargePercent >= 1.0;
                    if (!wasFullyCharged && isNowFullyCharged) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    fullyChargedStatus.put(playerId, isNowFullyCharged);
                    updateExplosionCharge(player, chargePercent);
                }
                else if (bowType.equals("SOUL")) {
                    double chargePercent = Math.min(1.0, chargeTime / chargeTargetMs);
                    chargeTimers.put(playerId, chargePercent);
                    boolean wasFullyCharged = fullyChargedStatus.getOrDefault(playerId, false);
                    boolean isNowFullyCharged = chargePercent >= 1.0;
                    if (!wasFullyCharged && isNowFullyCharged) {
                        player.playSound(player.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 0.7f);
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 0.5f);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                        Map<String, Object> parameters = specialBow.get().getParameters(item);
                        String targetType = "PLAYER";
                        double effectRange = 8.0;
                        if (parameters.containsKey("targetType") && parameters.get("targetType") instanceof String) {
                            targetType = (String) parameters.get("targetType");
                        }
                        if (parameters.containsKey("range") && parameters.get("range") instanceof Number) {
                            effectRange = ((Number) parameters.get("range")).doubleValue();
                        }
                        plugin.getLogger().info("Applying Soul Bow effects to player " + player.getName() +
                                " and targets in range " + effectRange + " - target type: " + targetType);
                        int entitiesAffected = 0;
                        for (Entity entity : player.getNearbyEntities(effectRange, effectRange, effectRange)) {
                            boolean isValidTarget = false;
                            if (targetType.equals("DEBUG") && entity.getType() == EntityType.IRON_GOLEM) {
                                isValidTarget = true;
                            } else if (targetType.equals("PLAYER") && entity instanceof Player) {
                                isValidTarget = true;
                            }
                            if (isValidTarget && entity instanceof LivingEntity && !entity.equals(player)) {
                                ((LivingEntity) entity).addPotionEffect(
                                        new PotionEffect(PotionEffectType.GLOWING, 200, 0)
                                );
                                entitiesAffected++;
                            }
                        }
                        plugin.getLogger().info("Applied glowing effect to " + entitiesAffected + " entities");
                    }
                    fullyChargedStatus.put(playerId, isNowFullyCharged);
                    updateSoulCharge(player, chargePercent);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); 
        chargingTasks.put(playerId, task);
    }
    private void stopCharging(Player player, boolean clearChargingData) {
        UUID playerId = player.getUniqueId();
        if (chargingTasks.containsKey(playerId)) {
            chargingTasks.get(playerId).cancel();
            chargingTasks.remove(playerId);
        }
        chargingStartTimes.remove(playerId);
        particlePositions.remove(playerId);
        chargingBowTypes.remove(playerId);
        chargeTimers.remove(playerId);
        if (clearChargingData) {
            visibleRings.remove(playerId);
            fullyChargedStatus.remove(playerId);
        }
    }
    private void updateYoimiyaRings(Player player, long chargeTime) {
        UUID playerId = player.getUniqueId();
        int ringsToShow = (int) Math.min(RING_COUNT, chargeTime / RING_APPEAR_INTERVAL);
        visibleRings.put(playerId, ringsToShow);
        int position = particlePositions.getOrDefault(playerId, 0);
        position = (position + 1) % 24; 
        particlePositions.put(playerId, position);
        double ringRadius = 0.5;
        double distance = 2.0;
        for (int ringType = 0; ringType < ringsToShow; ringType++) {
            for (int offset = 0; offset < 3; offset++) {
                int particlePos = (position + offset * 8) % 24; 
                drawParticleAtPosition(player, distance, ringRadius, 24, particlePos,
                        Particle.FLAME, ringType);
            }
        }
    }
    private void updateExplosionCharge(Player player, double chargePercent) {
        double ringRadius = 1.5; 
        double distance = 2.0;   
        int particlesPerHalf = 4; 
        int leftParticles = 0;
        int rightParticles = 0;
        if (chargePercent <= 0.5) {
            leftParticles = (int)(chargePercent * 2 * particlesPerHalf);
            rightParticles = (int)(chargePercent * 2 * particlesPerHalf);
        } else {
            leftParticles = particlesPerHalf;
            rightParticles = particlesPerHalf;
        }
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Vector perpendicular1 = getPerpendicular(direction); 
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize(); 
        Location center = eyeLocation.clone().add(direction.clone().multiply(distance));
        for (int i = 0; i < leftParticles; i++) {
            double angle = Math.PI + (Math.PI * i / particlesPerHalf);
            drawExplosionRingParticle(player, center, perpendicular1, perpendicular2, angle, ringRadius);
        }
        for (int i = 0; i < rightParticles; i++) {
            double angle = 0 + (Math.PI * i / particlesPerHalf);
            drawExplosionRingParticle(player, center, perpendicular1, perpendicular2, angle, ringRadius);
        }
    }
    private void drawExplosionRingParticle(Player player, Location center, Vector right, Vector up,
                                           double angle, double radius) {
        double x = radius * Math.cos(angle);
        double y = radius * Math.sin(angle);
        Vector offset = right.clone().multiply(x).add(up.clone().multiply(y));
        Location particleLocation = center.clone().add(offset);
        player.getWorld().spawnParticle(Particle.CLOUD, particleLocation, 1, 0, 0, 0, 0);
    }
    private void updateSoulCharge(Player player, double chargePercent) {
        double ringRadius = 1.5; 
        double distance = 2.0;   
        int totalParticles = 16; 
        int particlesToShow = (int)(chargePercent * totalParticles);
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Vector perpendicular1 = getPerpendicular(direction); 
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize(); 
        Location center = eyeLocation.clone().add(direction.clone().multiply(distance));
        for (int i = 0; i < particlesToShow; i++) {
            double angle = 2 * Math.PI * i / totalParticles;
            double x = ringRadius * Math.cos(angle);
            double y = ringRadius * Math.sin(angle);
            Vector offset = perpendicular1.clone().multiply(x).add(perpendicular2.clone().multiply(y));
            Location particleLocation = center.clone().add(offset);
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLocation, 1, 0, 0, 0, 0);
        }
        if (chargePercent >= 1.0) {
            if (random.nextDouble() < 0.3) {
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = 0.7 + random.nextDouble() * 0.5;
                Location particleLoc = player.getLocation().add(
                        Math.cos(angle) * dist,
                        1.0 + random.nextDouble() * 1.0,
                        Math.sin(angle) * dist
                );
                player.getWorld().spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        particleLoc,
                        1, 0, 0, 0, 0.01
                );
            }
        }
    }
    private void applyGlowingToNearbyTargets(Player shooter, String targetType, double range) {
        shooter.getNearbyEntities(range, range, range).stream()
                .filter(entity -> {
                    if (entity.equals(shooter)) return false;
                    if (targetType.equals("DEBUG") && entity.getType() == org.bukkit.entity.EntityType.IRON_GOLEM) {
                        return true;
                    } else if (targetType.equals("PLAYER") && entity instanceof Player) {
                        return true;
                    }
                    return false;
                })
                .forEach(entity -> {
                    if (entity instanceof org.bukkit.entity.LivingEntity) {
                        ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(
                                new PotionEffect(PotionEffectType.GLOWING, 200, 0)
                        );
                    }
                });
    }
    private void drawParticleAtPosition(Player player, double distance, double radius, int totalParticles,
                                        int position, Particle particleType, int ringType) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Vector perpendicular1 = getPerpendicular(direction);
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize();
        Location baseCenter = eyeLocation.clone().add(direction.clone().multiply(distance));
        Location center = baseCenter.clone();
        double offsetDistance = 1.0;
        switch (ringType) {
            case RING_TOP:
                center.add(perpendicular2.clone().multiply(offsetDistance));
                break;
            case RING_RIGHT:
                center.add(perpendicular1.clone().multiply(offsetDistance));
                break;
            case RING_BOTTOM:
                center.add(perpendicular2.clone().multiply(-offsetDistance));
                break;
            case RING_LEFT:
                center.add(perpendicular1.clone().multiply(-offsetDistance));
                break;
        }
        double angle = 2 * Math.PI * position / totalParticles;
        double x = radius * Math.cos(angle);
        double y = radius * Math.sin(angle);
        Vector offset = perpendicular1.clone().multiply(x).add(perpendicular2.clone().multiply(y));
        Location particleLocation = center.clone().add(offset);
        player.getWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0);
    }
    private Vector getPerpendicular(Vector vector) {
        if (Math.abs(vector.getY()) < 0.9) {
            return new Vector(0, 1, 0).crossProduct(vector).normalize();
        } else {
            return new Vector(1, 0, 0).crossProduct(vector).normalize();
        }
    }
}
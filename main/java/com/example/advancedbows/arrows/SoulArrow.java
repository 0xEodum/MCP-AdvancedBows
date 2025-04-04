package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class SoulArrow implements SpecialArrow {
    private final AdvancedBows plugin;
    private final Player shooter;
    private final String targetType;
    private final double beamLength;
    private final double fangRadius;
    private final int fangCount;
    private final Random random = new Random();
    private BukkitTask beamTask;
    private BukkitTask vexCleanupTask;
    private final List<Vex> spawnedVexes = new ArrayList<>();
    private static final double BEAM_STEP = 0.5; 
    private static final int BEAM_PARTICLES_PER_STEP = 3; 
    private static final long BEAM_DURATION_TICKS = 10; 
    public SoulArrow(AdvancedBows plugin, Player shooter, String targetType,
                     double beamLength, double fangRadius, int fangCount) {
        this.plugin = plugin;
        this.shooter = shooter;
        this.targetType = targetType;
        this.beamLength = beamLength;
        this.fangRadius = fangRadius;
        this.fangCount = fangCount;
        fireBeam();
    }
    private void fireBeam() {
        Location startLoc = shooter.getEyeLocation();
        World world = startLoc.getWorld();
        Vector direction = startLoc.getDirection();
        beamTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= BEAM_DURATION_TICKS) {
                    cancel();
                    return;
                }
                Entity hitEntity = null;
                Location hitLocation = null;
                double hitDistance = beamLength;
                for (double d = 0; d <= beamLength; d += 0.5) {
                    Location checkLoc = startLoc.clone().add(direction.clone().multiply(d));
                    for (Entity entity : world.getNearbyEntities(checkLoc, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity && !entity.equals(shooter) && isValidTarget(entity)) {
                            hitEntity = entity;
                            hitLocation = checkLoc;
                            hitDistance = d;
                            break;
                        }
                    }
                    if (hitEntity != null) {
                        break;
                    }
                }
                for (double d = 0; d <= (hitEntity != null ? hitDistance : beamLength); d += BEAM_STEP) {
                    Location particleLoc = startLoc.clone().add(direction.clone().multiply(d));
                    for (int i = 0; i < BEAM_PARTICLES_PER_STEP; i++) {
                        double offsetX = random.nextDouble() * 0.1 - 0.05;
                        double offsetY = random.nextDouble() * 0.1 - 0.05;
                        double offsetZ = random.nextDouble() * 0.1 - 0.05;
                        world.spawnParticle(
                                Particle.SOUL_FIRE_FLAME,
                                particleLoc.getX() + offsetX,
                                particleLoc.getY() + offsetY,
                                particleLoc.getZ() + offsetZ,
                                1, 0, 0, 0, 0
                        );
                    }
                }
                if (ticks == 1 && hitEntity != null && hitEntity instanceof LivingEntity) {
                    handleEntityHit((LivingEntity) hitEntity, hitLocation);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    private void handleEntityHit(LivingEntity entity, Location hitLocation) {
        double currentHealth = entity.getHealth();
        double newHealth = Math.max(1.0, currentHealth * 0.3); 
        entity.setHealth(newHealth);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2)); 
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2)); 
        spawnFangsCircle(entity.getLocation(), fangRadius, fangCount);
        spawnVexes(entity);
        World world = entity.getWorld();
        world.playSound(entity.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 0.5f);
        world.playSound(entity.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.8f);
        world.playSound(entity.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.7f);
    }
    private void spawnFangsCircle(Location center, double radius, int count) {
        World world = center.getWorld();
        for (int i = 0; i < count; i++) {
            double randomDistance = radius * Math.sqrt(random.nextDouble());
            double randomAngle = random.nextDouble() * 2 * Math.PI;
            double x = randomDistance * Math.cos(randomAngle);
            double z = randomDistance * Math.sin(randomAngle);
            Location fangLoc = center.clone().add(x, 0, z);
            if (random.nextDouble() < 0.3) {
                fangLoc.add((random.nextDouble() - 0.5) * 0.5, 0, (random.nextDouble() - 0.5) * 0.5);
            }
            fangLoc.setY(getSurfaceY(fangLoc));
            EvokerFangs fang = (EvokerFangs) world.spawnEntity(fangLoc, EntityType.EVOKER_FANGS);
            if (shooter != null) {
                fang.setOwner(shooter);
            }
        }
    }
    private double getSurfaceY(Location location) {
        Location checkLoc = location.clone();
        for (int y = 0; y > -5; y--) {
            checkLoc.setY(location.getY() + y);
            if (checkLoc.getBlock().getType().isSolid()) {
                return checkLoc.getY() + 1;
            }
        }
        for (int y = 0; y < 5; y++) {
            checkLoc.setY(location.getY() + y);
            if (checkLoc.getBlock().getType().isSolid()) {
                return checkLoc.getY() + 1;
            }
        }
        return location.getY();
    }
    private void spawnVexes(LivingEntity target) {
        World world = target.getWorld();
        Location spawnLoc = target.getLocation().add(0, 3, 0);
        for (int i = 0; i < 3; i++) {
            double offsetX = random.nextDouble() * 2 - 1;
            double offsetY = random.nextDouble() + 1;
            double offsetZ = random.nextDouble() * 2 - 1;
            Location vexLoc = spawnLoc.clone().add(offsetX, offsetY, offsetZ);
            Vex vex = (Vex) world.spawnEntity(vexLoc, EntityType.VEX);
            vex.setTarget(target);
            vex.setGlowing(true);
            spawnedVexes.add(vex);
        }
        vexCleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Vex vex : spawnedVexes) {
                    if (vex != null && vex.isValid()) {
                        vex.remove();
                    }
                }
                spawnedVexes.clear();
            }
        }.runTaskLater(plugin, 200L); 
    }
    private boolean isValidTarget(Entity entity) {
        if (targetType.equals("DEBUG") && entity.getType() == EntityType.IRON_GOLEM) {
            return true;
        } else if (targetType.equals("PLAYER") && entity instanceof Player) {
            return true;
        }
        return false;
    }
    @Override
    public org.bukkit.entity.Arrow getArrow() {
        return null;
    }
    @Override
    public String getType() {
        return "SOUL";
    }
    @Override
    public void onHit() {
    }
    @Override
    public void remove() {
        if (beamTask != null && !beamTask.isCancelled()) {
            beamTask.cancel();
        }
        if (vexCleanupTask != null && !vexCleanupTask.isCancelled()) {
            vexCleanupTask.cancel();
        }
        for (Vex vex : spawnedVexes) {
            if (vex != null && vex.isValid()) {
                vex.remove();
            }
        }
        spawnedVexes.clear();
    }
}
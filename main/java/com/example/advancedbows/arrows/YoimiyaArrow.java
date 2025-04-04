package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.effects.ArrowTrailEffect;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
public class YoimiyaArrow extends TargetingArrow {
    private final double pursuit;
    private ArrowTrailEffect fireTrail;
    private BukkitTask timeoutTask;
    private final boolean isMainArrow;
    private final boolean isChargedMainArrow;
    private static final long TARGET_TIMEOUT_TICKS = 80; 
    private static final String YOIMIYA_DAMAGE_KEY = "yoimiya_damage_marker";
    public YoimiyaArrow(AdvancedBows plugin, Arrow arrow, Player shooter, String targetType,
                        double range, double pursuit, boolean isMainArrow, boolean isChargedMainArrow) {
        super(plugin, arrow, shooter, targetType, range);
        this.pursuit = pursuit;
        this.isMainArrow = isMainArrow;
        this.isChargedMainArrow = isChargedMainArrow;
        if (!isMainArrow || isChargedMainArrow) {
            arrow.setFireTicks(300); 
            this.fireTrail = new ArrowTrailEffect(plugin, arrow, Particle.DRIP_LAVA, Color.RED);
        }
        if (isMainArrow) {
            arrow.setGravity(true);
        } else {
            arrow.setGravity(false);
        }
        arrow.getPersistentDataContainer().set(
                plugin.getNamespacedKey(YOIMIYA_DAMAGE_KEY),
                PersistentDataType.BYTE,
                (byte) 0
        );
        if (!isMainArrow) {
            startTimeoutCountdown();
        }
        plugin.getLogger().info("Created YoimiyaArrow: target=" + targetType + ", range=" + range +
                ", pursuit=" + pursuit + ", isMainArrow=" + isMainArrow +
                ", isChargedMainArrow=" + isChargedMainArrow);
    }
    private void startTimeoutCountdown() {
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isTargetFound && arrow != null && arrow.isValid() && !arrow.isDead()) {
                    plugin.getLogger().info("Yoimiya arrow timed out without finding target, exploding");
                    explodeArrow();
                }
                cancel();
            }
        }.runTaskLater(plugin, TARGET_TIMEOUT_TICKS);
    }
    @Override
    protected void onTargetTracking() {
        if (!isMainArrow) {
            updateArrowDirection();
        }
        if (fireTrail != null) {
            fireTrail.playEffect();
        }
    }
    @Override
    protected void onNormalTracking() {
        if (fireTrail != null) {
            fireTrail.playEffect();
        }
    }
    @Override
    protected void findTarget() {
        if (isMainArrow) {
            return;
        }
        super.findTarget();
        if (isTargetFound && timeoutTask != null && !timeoutTask.isCancelled()) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
    }
    private void updateArrowDirection() {
        if (target == null) return;
        Location arrowLoc = arrow.getLocation();
        Vector arrowVel = arrow.getVelocity();
        double speed = arrowVel.length();
        Vector toTarget = target.getLocation().clone().add(0, 0.5, 0).subtract(arrowLoc).toVector().normalize();
        Vector newDir = arrowVel.normalize().multiply(1 - pursuit).add(toTarget.multiply(pursuit)).normalize();
        arrow.setVelocity(newDir.multiply(speed));
    }
    public void explodeArrow() {
        if ((!isMainArrow || isChargedMainArrow) && arrow != null && arrow.isValid() && !arrow.isDead()) {
            Location loc = arrow.getLocation();
            Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.fromRGB(255, 69, 0)) 
                    .withColor(Color.fromRGB(255, 165, 0)) 
                    .withColor(Color.fromRGB(255, 215, 0)) 
                    .withFlicker()
                    .build();
            meta.addEffect(effect);
            meta.setPower(0); 
            firework.setFireworkMeta(meta);
            firework.detonate();
            arrow.remove();
        }
        cleanup();
    }
    @Override
    public String getType() {
        return "YOIMIYA";
    }
    @Override
    public void onHit() {
        if (arrow == null || !arrow.isValid() || arrow.isDead()) return;
        Entity hitEntity = arrow.getNearbyEntities(0.5, 0.5, 0.5).stream()
                .filter(e -> !(e instanceof Projectile) && !(e instanceof Player && e.equals(shooter)))
                .sorted((e1, e2) ->
                        Double.compare(e1.getLocation().distanceSquared(arrow.getLocation()),
                                e2.getLocation().distanceSquared(arrow.getLocation())))
                .findFirst().orElse(null);
        if (!isMainArrow || isChargedMainArrow) {
            explodeArrow();
            plugin.getParticleManager().createImpactEffect(arrow.getLocation(),
                    Particle.FLAME, Color.ORANGE);
            arrow.getWorld().spawnParticle(
                    Particle.LAVA,
                    arrow.getLocation(),
                    10,  
                    0.3, 0.3, 0.3,  
                    0.1  
            );
        }
        cleanup();
    }
    private void cleanup() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        if (timeoutTask != null && !timeoutTask.isCancelled()) {
            timeoutTask.cancel();
        }
    }
    @Override
    public void remove() {
        cleanup();
        if (arrow != null && arrow.isValid() && !arrow.isDead()) {
            arrow.remove();
        }
    }
}
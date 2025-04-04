package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.effects.ArrowTrailEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
public class ExplosionArrow implements SpecialArrow {
    private final AdvancedBows plugin;
    private final Arrow arrow;
    private final Player shooter;
    private final double explosionPower;
    private final boolean isFullyCharged;
    private BukkitTask task;
    private ArrowTrailEffect redTrail;
    private ArrowTrailEffect sparkTrail;
    public ExplosionArrow(AdvancedBows plugin, Arrow arrow, Player shooter, double explosionPower, boolean isFullyCharged) {
        this.plugin = plugin;
        this.arrow = arrow;
        this.shooter = shooter;
        this.explosionPower = explosionPower;
        this.isFullyCharged = isFullyCharged;
        plugin.getArrowManager().registerArrow(this);
        if (isFullyCharged) {
            this.redTrail = new ArrowTrailEffect(plugin, arrow, Particle.REDSTONE, Color.RED);
            this.sparkTrail = new ArrowTrailEffect(plugin, arrow, Particle.FIREWORKS_SPARK, null);
        }
        plugin.getLogger().info("Created ExplosionArrow: power=" + explosionPower + ", fullCharge=" + isFullyCharged);
        startTracking();
    }
    private void startTracking() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    cancel();
                    return;
                }
                if (isFullyCharged) {
                    redTrail.playEffect();
                    sparkTrail.playEffect();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    @Override
    public Arrow getArrow() {
        return arrow;
    }
    @Override
    public String getType() {
        return "EXPLOSION";
    }
    @Override
    public void onHit() {
        if (isFullyCharged) {
            createExplosion();
        }
        plugin.getParticleManager().createImpactEffect(arrow.getLocation(),
                isFullyCharged ? Particle.EXPLOSION_LARGE : Particle.CLOUD,
                isFullyCharged ? Color.RED : Color.WHITE);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
    private void createExplosion() {
        Location location = arrow.getLocation();
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        location.getWorld().spawnParticle(
                Particle.EXPLOSION_LARGE,
                location,
                10,  
                1.0, 1.0, 1.0,  
                0.1  
        );
        location.getWorld().spawnParticle(
                Particle.FIREWORKS_SPARK,
                location,
                40,  
                1.5, 1.5, 1.5,  
                0.3  
        );
        location.getWorld().spawnParticle(
                Particle.FLAME,
                location,
                30,  
                1.0, 1.0, 1.0,  
                0.1  
        );
        double radius = explosionPower * 2.5; 
        double damage = explosionPower * 4.0; 
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity.equals(shooter)) continue; 
            double distance = entity.getLocation().distance(location);
            if (distance <= radius) {
                double falloff = 1.0 - (distance / radius);
                double actualDamage = damage * falloff;
                if (entity instanceof org.bukkit.entity.Damageable) {
                    ((org.bukkit.entity.Damageable) entity).damage(actualDamage, shooter);
                }
            }
        }
        boolean breakBlocks = false;
        boolean setFire = false;
        float explosionSize = (float) Math.min(explosionPower, 4.0f);
        location.getWorld().createExplosion(
                location.getX(), location.getY(), location.getZ(),
                explosionSize,
                setFire,
                breakBlocks
        );
        remove();
    }
    @Override
    public void remove() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        if (arrow != null && arrow.isValid() && !arrow.isDead()) {
            arrow.remove();
        }
    }
}
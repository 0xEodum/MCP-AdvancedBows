package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.effects.ArrowTrailEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.Random;
public class EndArrow extends TargetingArrow {
    private static final Random RANDOM = new Random();
    private static final int TELEPORT_DELAY_TICKS = 10; 
    private static final int MAX_LIFETIME_TICKS = 200; 
    private final double teleportRadius;
    private final double teleportHeight;
    private ArrowTrailEffect normalTrail;
    private BukkitTask lifetimeTask;
    private int lifetimeTicks = 0;
    private boolean isDisappeared = false;
    private boolean isTeleporting = false;
    public EndArrow(AdvancedBows plugin, Arrow arrow, Player shooter, String targetType,
                    double range, double teleportRadius, double teleportHeight) {
        super(plugin, arrow, shooter, targetType, range);
        this.teleportRadius = teleportRadius;
        this.teleportHeight = teleportHeight;
        this.normalTrail = new ArrowTrailEffect(plugin, arrow, Particle.DRAGON_BREATH, Color.PURPLE);
        startLifetimeCountdown();
        plugin.getLogger().info("Created EndArrow: target=" + targetType + ", range=" + range +
                ", teleportRadius=" + teleportRadius + ", teleportHeight=" + teleportHeight);
    }
    private void startLifetimeCountdown() {
        lifetimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                lifetimeTicks++;
                if (lifetimeTicks >= MAX_LIFETIME_TICKS) {
                    if (arrow != null && arrow.isValid() && !arrow.isDead()) {
                        createEndBurstEffect(arrow.getLocation());
                        arrow.remove();
                    }
                    if (task != null && !task.isCancelled()) {
                        task.cancel();
                    }
                    cancel(); 
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    @Override
    protected void onTargetTracking() {
        double distanceToTarget = getDistanceToTarget();
        if (!isTeleporting && distanceToTarget > 0 && distanceToTarget <= range) {
            startTeleportSequence();
        }
        if (!isDisappeared) {
            normalTrail.playEffect();
        }
    }
    @Override
    protected void onNormalTracking() {
        if (!isDisappeared) {
            normalTrail.playEffect();
        }
    }
    private void startTeleportSequence() {
        isTeleporting = true;
        isDisappeared = true;
        createEndBurstEffect(arrow.getLocation());
        arrow.setGravity(false);
        arrow.setVelocity(new Vector(0, 0, 0));
        arrow.teleport(arrow.getLocation().clone().add(0, 1000, 0)); 
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target != null && target.isValid() && !target.isDead()) {
                    Location targetLoc = target.getLocation();
                    double angle = RANDOM.nextDouble() * Math.PI * 2; 
                    double x = Math.cos(angle) * teleportRadius;
                    double z = Math.sin(angle) * teleportRadius;
                    Location teleportLoc = targetLoc.clone().add(x, teleportHeight, z);
                    arrow.teleport(teleportLoc);
                    Vector direction = targetLoc.clone().add(0, 0.5, 0)
                            .subtract(teleportLoc).toVector().normalize();
                    double speed = 2.0; 
                    arrow.setVelocity(direction.multiply(speed));
                    createEndBurstEffect(teleportLoc);
                    isDisappeared = false;
                } else {
                    arrow.teleport(arrow.getLocation().clone().subtract(0, 1000, 0));
                    arrow.setVelocity(new Vector(0, 0, 0));
                    isDisappeared = false;
                }
            }
        }.runTaskLater(plugin, TELEPORT_DELAY_TICKS);
    }
    private void createEndBurstEffect(Location location) {
        location.getWorld().spawnParticle(
                Particle.PORTAL,
                location,
                30,  
                0.3, 0.3, 0.3,  
                0.5  
        );
        location.getWorld().spawnParticle(
                Particle.DRAGON_BREATH,
                location,
                15,  
                0.2, 0.2, 0.2,  
                0.1  
        );
    }
    @Override
    public String getType() {
        return "END";
    }
    @Override
    public void onHit() {
        plugin.getParticleManager().createImpactEffect(arrow.getLocation(),
                Particle.PORTAL, Color.PURPLE);
        cleanup();
    }
    @Override
    public void remove() {
        cleanup();
        if (arrow != null && arrow.isValid() && !arrow.isDead()) {
            arrow.remove();
        }
    }
    private void cleanup() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        if (lifetimeTask != null && !lifetimeTask.isCancelled()) {
            lifetimeTask.cancel();
        }
    }
}
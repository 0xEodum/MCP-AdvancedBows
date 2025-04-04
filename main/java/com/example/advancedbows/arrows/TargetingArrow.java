package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
public abstract class TargetingArrow implements SpecialArrow {
    protected final AdvancedBows plugin;
    protected final Arrow arrow;
    protected final Player shooter;
    protected final String targetType;
    protected final double range;
    protected BukkitTask task;
    protected Entity target;
    protected boolean isTargetFound = false;
    public TargetingArrow(AdvancedBows plugin, Arrow arrow, Player shooter, String targetType, double range) {
        this.plugin = plugin;
        this.arrow = arrow;
        this.shooter = shooter;
        this.targetType = targetType;
        this.range = range;
        plugin.getArrowManager().registerArrow(this);
        startTracking();
    }
    protected void startTracking() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    cancel();
                    return;
                }
                if (!isTargetFound) {
                    findTarget();
                }
                if (isTargetFound && target != null && target.isValid() && !target.isDead()) {
                    onTargetTracking();
                } else {
                    onNormalTracking();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    protected void findTarget() {
        List<Entity> potentialTargets = arrow.getNearbyEntities(range, range, range)
                .stream()
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(arrow.getLocation())))
                .collect(Collectors.toList());
        if (!potentialTargets.isEmpty()) {
            target = potentialTargets.get(0);
            isTargetFound = true;
            plugin.getLogger().info(getType() + " found target: " + target.getType() + " at distance " +
                    arrow.getLocation().distance(target.getLocation()));
        }
    }
    protected boolean isValidTarget(Entity entity) {
        if (entity.equals(shooter)) return false;
        if (targetType.equals("DEBUG") && entity.getType() == EntityType.IRON_GOLEM) {
            return true;
        } else if (targetType.equals("PLAYER") && entity.getType() == EntityType.PLAYER) {
            return true;
        }
        return false;
    }
    protected double getDistanceToTarget() {
        if (target == null || !target.isValid() || arrow == null || !arrow.isValid()) {
            return -1;
        }
        return arrow.getLocation().distance(target.getLocation());
    }
    protected abstract void onTargetTracking();
    protected abstract void onNormalTracking();
    @Override
    public Arrow getArrow() {
        return arrow;
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
    public Entity getTarget() {
        return target;
    }
}
package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.effects.ArrowTrailEffect;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
public class MultishotArrow implements SpecialArrow {
    private final AdvancedBows plugin;
    private final Arrow arrow;
    private final Arrow parentArrow;
    private final double maxDeviation;
    private final ArrowTrailEffect trailEffect;
    private BukkitTask task;
    public MultishotArrow(AdvancedBows plugin, Arrow arrow, Arrow parentArrow, double maxDeviation) {
        this.plugin = plugin;
        this.arrow = arrow;
        this.parentArrow = parentArrow;
        this.maxDeviation = maxDeviation;
        this.trailEffect = new ArrowTrailEffect(plugin, arrow, Particle.CRIT, null);
        plugin.getArrowManager().registerArrow(this);
        plugin.getLogger().info("Created MultishotArrow");
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
                trailEffect.playEffect();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    @Override
    public Arrow getArrow() {
        return arrow;
    }
    @Override
    public String getType() {
        return "MULTISHOT";
    }
    @Override
    public void onHit() {
        plugin.getParticleManager().createImpactEffect(arrow.getLocation(), Particle.FLAME, Color.ORANGE);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
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
package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.effects.ArrowTrailEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
public class HomingArrow extends TargetingArrow {
    private final double pursuit;
    private ArrowTrailEffect normalTrail;
    private ArrowTrailEffect homingTrail;
    public HomingArrow(AdvancedBows plugin, Arrow arrow, Player shooter, String targetType, double range, double pursuit) {
        super(plugin, arrow, shooter, targetType, range);
        this.pursuit = pursuit;
        this.normalTrail = new ArrowTrailEffect(plugin, arrow, Particle.END_ROD, null);
        this.homingTrail = new ArrowTrailEffect(plugin, arrow, Particle.DRIP_LAVA, Color.RED);
        plugin.getLogger().info("Created HomingArrow: target=" + targetType + ", range=" + range + ", pursuit=" + pursuit);
    }
    @Override
    protected void onTargetTracking() {
        updateArrowDirection();
        homingTrail.playEffect();
    }
    @Override
    protected void onNormalTracking() {
        normalTrail.playEffect();
    }
    void updateArrowDirection() {
        if (target == null) return;
        Location arrowLoc = arrow.getLocation();
        Vector arrowVel = arrow.getVelocity();
        double speed = arrowVel.length();
        Vector toTarget = target.getLocation().clone().add(0, 0.5, 0).subtract(arrowLoc).toVector().normalize();
        Vector newDir = arrowVel.normalize().multiply(1 - pursuit).add(toTarget.multiply(pursuit)).normalize();
        arrow.setVelocity(newDir.multiply(speed));
    }
    @Override
    public String getType() {
        return "HOMING";
    }
    @Override
    public void onHit() {
        plugin.getParticleManager().createImpactEffect(arrow.getLocation(),
                isTargetFound ? Particle.LAVA : Particle.CLOUD,
                isTargetFound ? Color.RED : Color.WHITE);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}
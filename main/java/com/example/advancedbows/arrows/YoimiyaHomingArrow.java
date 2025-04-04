package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.effects.ArrowTrailEffect;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
public class YoimiyaHomingArrow extends HomingArrow {
    private final ArrowTrailEffect fireTrail;
    public YoimiyaHomingArrow(AdvancedBows plugin, Arrow arrow, Player shooter, String targetType, double range, double pursuit) {
        super(plugin, arrow, shooter, targetType, range, pursuit);
        this.fireTrail = new ArrowTrailEffect(plugin, arrow, Particle.DRIP_LAVA, Color.RED);
        plugin.getLogger().info("Created YoimiyaHomingArrow: target=" + targetType + ", range=" + range + ", pursuit=" + pursuit);
    }
    @Override
    protected void onTargetTracking() {
        updateArrowDirection();
        fireTrail.playEffect();
    }
    @Override
    protected void onNormalTracking() {
        fireTrail.playEffect();
    }
    @Override
    public String getType() {
        return "YOIMIYA";
    }
}
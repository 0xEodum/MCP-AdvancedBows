package com.example.advancedbows.effects;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
public class ArrowTrailEffect implements ParticleEffect {
    private final AdvancedBows plugin;
    private final Arrow arrow;
    private final Particle particleType;
    private final Color particleColor;
    private final int particleCount;
    private final double particleSpeed;
    private final double particleOffset;
    public ArrowTrailEffect(AdvancedBows plugin, Arrow arrow, Particle particleType, Color particleColor) {
        this(plugin, arrow, particleType, particleColor, 1, 0.05, 0.0);
    }
    public ArrowTrailEffect(AdvancedBows plugin, Arrow arrow, Particle particleType, Color particleColor,
                            int particleCount, double particleSpeed, double particleOffset) {
        this.plugin = plugin;
        this.arrow = arrow;
        this.particleType = particleType;
        this.particleColor = particleColor;
        this.particleCount = particleCount;
        this.particleSpeed = particleSpeed;
        this.particleOffset = particleOffset;
    }
    @Override
    public void playEffect() {
        if (arrow == null || !arrow.isValid() || arrow.isDead()) {
            return;
        }
        Location location = arrow.getLocation();
        if (particleType == Particle.REDSTONE && particleColor != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(
                    particleColor,
                    1.0F  
            );
            location.getWorld().spawnParticle(
                    particleType,
                    location,
                    particleCount,
                    particleOffset, particleOffset, particleOffset,  
                    particleSpeed,
                    dustOptions
            );
        } else {
            location.getWorld().spawnParticle(
                    particleType,
                    location,
                    particleCount,
                    particleOffset, particleOffset, particleOffset,  
                    particleSpeed
            );
        }
    }
    @Override
    public Location getLocation() {
        return arrow.getLocation();
    }
}
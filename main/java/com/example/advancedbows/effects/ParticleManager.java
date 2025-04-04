package com.example.advancedbows.effects;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.List;
public class ParticleManager {
    private final AdvancedBows plugin;
    private final List<ParticleEffect> activeEffects;
    public ParticleManager(AdvancedBows plugin) {
        this.plugin = plugin;
        this.activeEffects = new ArrayList<>();
    }
    public void registerEffect(ParticleEffect effect) {
        activeEffects.add(effect);
    }
    public void unregisterEffect(ParticleEffect effect) {
        activeEffects.remove(effect);
    }
    public void createImpactEffect(Location location, Particle particleType, Color particleColor) {
        final int burstCount = 30;
        final double speed = 0.1;
        final double radius = 0.5;
        if (particleType == Particle.REDSTONE && particleColor != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(
                    particleColor,
                    1.0F  
            );
            location.getWorld().spawnParticle(
                    particleType,
                    location,
                    burstCount,
                    radius, radius, radius,  
                    speed,
                    dustOptions
            );
        } else {
            location.getWorld().spawnParticle(
                    particleType,
                    location,
                    burstCount,
                    radius, radius, radius,  
                    speed
            );
        }
    }
    public void createCircleEffect(Location location, Particle particleType, Color particleColor, double radius) {
        final int points = 20;
        final double speed = 0.0;
        new BukkitRunnable() {
            double angle = 0;
            int step = 0;
            final int maxSteps = 3;
            @Override
            public void run() {
                for (int i = 0; i < points / maxSteps; i++) {
                    angle += 2 * Math.PI / points;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Location particleLoc = location.clone().add(x, 0, z);
                    if (particleType == Particle.REDSTONE && particleColor != null) {
                        Particle.DustOptions dustOptions = new Particle.DustOptions(
                                particleColor,
                                1.0F  
                        );
                        particleLoc.getWorld().spawnParticle(
                                particleType,
                                particleLoc,
                                1,
                                0, 0, 0,  
                                speed,
                                dustOptions
                        );
                    } else {
                        particleLoc.getWorld().spawnParticle(
                                particleType,
                                particleLoc,
                                1,
                                0, 0, 0,  
                                speed
                        );
                    }
                }
                step++;
                if (step >= maxSteps) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    public void cleanUp() {
        activeEffects.clear();
    }
}
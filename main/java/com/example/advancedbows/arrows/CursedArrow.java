package com.example.advancedbows.arrows;

import com.example.advancedbows.AdvancedBows;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CursedArrow implements SpecialArrow {
    private final AdvancedBows plugin;
    private final Player shooter;
    private final String targetType;
    private final double range;
    private final double damagePercent;
    private final int effectDuration;
    private final int projectileCount;
    private final boolean isFullyCharged;

    private final Random random = new Random();
    private BukkitTask projectileTask;
    private final List<CurseProjectile> projectiles = new ArrayList<>();

    private static final double INITIAL_RADIUS = 1.5;
    private static final double CONTRACTED_RADIUS = 0.5;
    private static final double PROJECTILE_SPEED = 6.0;
    private static final int MAX_LIFETIME_TICKS = 200;
    private static final PotionEffectType[] DEBUFF_EFFECTS = {
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER,
            PotionEffectType.BLINDNESS,
            PotionEffectType.LEVITATION
    };

    private static final int[] EFFECT_AMPLIFIERS = {
            0,
            1,
            1,
            1,
            1,
            0,
            0,
            4
    };

    public CursedArrow(AdvancedBows plugin, Player shooter, String targetType,
                       double range, double damagePercent, int effectDuration,
                       int projectileCount, boolean isFullyCharged) {
        this.plugin = plugin;
        this.shooter = shooter;
        this.targetType = targetType;
        this.range = range;
        this.damagePercent = damagePercent;
        this.effectDuration = effectDuration;
        this.projectileCount = Math.min(projectileCount, 8);
        this.isFullyCharged = isFullyCharged;

        plugin.getArrowManager().registerArrow(this);
        plugin.getLogger().info("Created CursedArrow: target=" + targetType +
                ", range=" + range + ", damagePercent=" + damagePercent +
                ", projectiles=" + projectileCount);

        initializeProjectiles();
        startProjectileMovement();
    }

    private void initializeProjectiles() {
        Location eyeLocation = shooter.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Vector perpendicular1 = getPerpendicular(direction);
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize();

        Location baseCenter = eyeLocation.clone().add(direction.clone().multiply(2.0));

        for (int i = 0; i < projectileCount; i++) {
            double angle = 2 * Math.PI * i / projectileCount;
            double x = INITIAL_RADIUS * Math.cos(angle);
            double y = INITIAL_RADIUS * Math.sin(angle);

            Vector offset = perpendicular1.clone().multiply(x).add(perpendicular2.clone().multiply(y));
            Location projectileLocation = baseCenter.clone().add(offset);

            int delayTicks = i * 5;

            projectiles.add(new CurseProjectile(
                    projectileLocation,
                    direction.clone(),
                    i % DEBUFF_EFFECTS.length,
                    delayTicks
            ));

            shooter.getWorld().spawnParticle(
                    Particle.SPELL_WITCH,
                    projectileLocation,
                    5,
                    0.0, 0.0, 0.0,
                    0.05
            );
        }

        shooter.getWorld().playSound(
                shooter.getLocation(),
                Sound.ENTITY_WITCH_AMBIENT,
                1.0f,
                1.0f
        );
    }

    private void startProjectileMovement() {
        projectileTask = new BukkitRunnable() {
            int ticks = 0;
            boolean contracted = false;

            @Override
            public void run() {
                ticks++;

                if (!contracted && ticks >= 5) {
                    contractFormation();
                    contracted = true;
                }

                if (ticks >= MAX_LIFETIME_TICKS) {
                    cleanup();
                    return;
                }

                moveProjectiles();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void contractFormation() {
        Location eyeLocation = shooter.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Vector perpendicular1 = getPerpendicular(direction);
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize();

        Location baseCenter = eyeLocation.clone().add(direction.clone().multiply(2.0));

        for (int i = 0; i < projectiles.size(); i++) {
            CurseProjectile projectile = projectiles.get(i);

            double angle = 2 * Math.PI * i / projectiles.size();
            double x = CONTRACTED_RADIUS * Math.cos(angle);
            double y = CONTRACTED_RADIUS * Math.sin(angle);

            Vector offset = perpendicular1.clone().multiply(x).add(perpendicular2.clone().multiply(y));
            Location newLocation = baseCenter.clone().add(offset);

            projectile.location = newLocation;
            projectile.direction = direction.clone().normalize();
        }
    }

    private void moveProjectiles() {
        List<CurseProjectile> projectilesToRemove = new ArrayList<>();

        for (CurseProjectile projectile : projectiles) {
            if (projectile.delayTicks > 0) {
                projectile.delayTicks--;
                continue;
            }

            Collection<Entity> nearbyEntities = projectile.location.getWorld().getNearbyEntities(
                    projectile.location, range, range, range);

            Entity nearestTarget = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Entity entity : nearbyEntities) {
                if (isValidTarget(entity)) {
                    double distance = entity.getLocation().distance(projectile.location);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestTarget = entity;
                    }
                }
            }

            // If there's a target and it's within range, adjust direction
            boolean targetInRange = nearestTarget != null && nearestDistance <= range;
            if (targetInRange) {
                Vector toTarget = nearestTarget.getLocation().clone()
                        .add(0, 0.5, 0) // Target the center of the entity
                        .subtract(projectile.location)
                        .toVector()
                        .normalize();

                // Gradual homing based on distance (more aggressive homing when closer)
                double homingFactor = Math.min(1.0, 0.3 + (1.0 - nearestDistance / range) * 0.6);
                projectile.direction = projectile.direction.multiply(1 - homingFactor)
                        .add(toTarget.multiply(homingFactor))
                        .normalize();
            }

            // Calculate new position
            double distancePerTick = PROJECTILE_SPEED / 20.0; // Convert from blocks/sec to blocks/tick
            Location nextLocation = projectile.location.clone().add(projectile.direction.clone().multiply(distancePerTick));

            // Check for block collision
            if (wouldHitBlock(projectile.location, nextLocation)) {
                // Hit a wall - terminate projectile with a particle effect
                projectile.location.getWorld().spawnParticle(
                        Particle.SPELL_WITCH,
                        projectile.location,
                        15,
                        0.1, 0.1, 0.1,
                        0.05
                );
                projectile.location.getWorld().playSound(
                        projectile.location,
                        Sound.BLOCK_GLASS_BREAK,
                        1.0f,
                        1.0f
                );
                projectilesToRemove.add(projectile);
                continue;
            }

            projectile.location = nextLocation;

            if (nearestTarget != null && projectile.location.distance(nearestTarget.getLocation()) < 1.0) {
                if (nearestTarget instanceof LivingEntity) {
                    onHitEntity((LivingEntity) nearestTarget, projectile);
                    projectilesToRemove.add(projectile);
                    continue;
                }
            }

            projectile.location.getWorld().spawnParticle(
                    Particle.SPELL_WITCH,
                    projectile.location,
                    5,
                    0.1, 0.1, 0.1,
                    0.01
            );
        }

        projectiles.removeAll(projectilesToRemove);

        if (projectiles.isEmpty()) {
            cleanup();
        }
    }

    private void onHitEntity(LivingEntity entity, CurseProjectile projectile) {
        double maxHealth = entity.getMaxHealth();
        double damage = maxHealth * (damagePercent / 100.0);

        entity.damage(damage, shooter);

        int effectIndex = projectile.effectIndex;
        PotionEffectType effectType = DEBUFF_EFFECTS[effectIndex];
        int amplifier = EFFECT_AMPLIFIERS[effectIndex];

        entity.addPotionEffect(new PotionEffect(
                effectType,
                effectDuration * 20,
                amplifier
        ));

        entity.getWorld().spawnParticle(
                Particle.SPELL_WITCH,
                entity.getLocation().add(0, 1, 0),
                20,
                0.5, 0.5, 0.5,
                0.1
        );

        entity.getWorld().playSound(
                entity.getLocation(),
                Sound.ENTITY_WITCH_HURT,
                1.0f,
                1.0f
        );

        plugin.getLogger().info("Cursed projectile hit entity with effect: " + effectType.getName() +
                " (level " + (amplifier + 1) + "), dealing " + damage + " damage.");
    }

    private boolean isValidTarget(Entity entity) {
        if (entity.equals(shooter)) return false;

        if (targetType.equals("DEBUG")) {
            return entity.getType() == EntityType.IRON_GOLEM;
        } else if (targetType.equals("PLAYER")) {
            return entity instanceof Player && !entity.equals(shooter);
        }

        return false;
    }

    private Vector getPerpendicular(Vector vector) {
        if (Math.abs(vector.getY()) < 0.9) {
            return new Vector(0, 1, 0).crossProduct(vector).normalize();
        } else {
            return new Vector(1, 0, 0).crossProduct(vector).normalize();
        }
    }

    private void cleanup() {
        if (projectileTask != null && !projectileTask.isCancelled()) {
            projectileTask.cancel();
        }

        projectiles.clear();
    }

    @Override
    public org.bukkit.entity.Arrow getArrow() {
        return null;
    }

    @Override
    public String getType() {
        return "CURSED";
    }

    @Override
    public void onHit() {
    }

    @Override
    public void remove() {
        cleanup();
    }

    private boolean wouldHitBlock(Location from, Location to) {
        if (!from.getWorld().equals(to.getWorld())) {
            return false;
        }

        Vector direction = to.clone().subtract(from).toVector();
        double distance = direction.length();

        if (distance <= 0) {
            return false;
        }

        direction.normalize();

        double step = 0.25;
        int steps = (int) Math.ceil(distance / step);

        for (int i = 0; i < steps; i++) {
            Location checkLoc = from.clone().add(direction.clone().multiply(i * step));

            if (checkLoc.getBlock().getType().isSolid()) {
                return true;
            }
        }

        return false;
    }

    private class CurseProjectile {
        Location location;
        Vector direction;
        int effectIndex;
        int delayTicks;

        CurseProjectile(Location location, Vector direction, int effectIndex, int delayTicks) {
            this.location = location;
            this.direction = direction;
            this.effectIndex = effectIndex;
            this.delayTicks = delayTicks;
        }
    }
}
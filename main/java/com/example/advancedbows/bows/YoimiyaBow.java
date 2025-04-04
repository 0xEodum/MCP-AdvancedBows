package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.HomingArrow;
import com.example.advancedbows.arrows.TargetingArrow;
import com.example.advancedbows.arrows.YoimiyaArrow;
import com.example.advancedbows.arrows.YoimiyaHomingArrow;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class YoimiyaBow extends AbstractBow {
    private final NamespacedKey targetTypeKey;
    private final NamespacedKey rangeKey;
    private final NamespacedKey pursuitKey;
    public YoimiyaBow(AdvancedBows plugin) {
        super(plugin);
        this.targetTypeKey = new NamespacedKey(plugin, "yoimiya_target_type");
        this.rangeKey = new NamespacedKey(plugin, "yoimiya_range");
        this.pursuitKey = new NamespacedKey(plugin, "yoimiya_pursuit");
    }
    @Override
    public String getType() {
        return "YOIMIYA";
    }
    @Override
    protected String getDisplayName() {
        return ChatColor.GOLD + "Yoimiya's Bow";
    }
    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        String targetType = getTargetType(parameters);
        double range = getRange(parameters);
        double pursuit = getPursuit(parameters);
        lore.add(ChatColor.GRAY + "Target: " + ChatColor.WHITE + targetType);
        lore.add(ChatColor.GRAY + "Range: " + ChatColor.WHITE + range + " blocks");
        lore.add(ChatColor.GRAY + "Pursuit: " + ChatColor.WHITE + pursuit);
        lore.add("");
        lore.add(ChatColor.GOLD + "» " + ChatColor.RED + "Creates particle rings while charging");
        lore.add(ChatColor.GOLD + "» " + ChatColor.RED + "Fires homing fire arrows from rings");
    }
    @Override
    protected void storeParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        container.set(targetTypeKey, PersistentDataType.STRING, getTargetType(parameters));
        container.set(rangeKey, PersistentDataType.DOUBLE, getRange(parameters));
        container.set(pursuitKey, PersistentDataType.DOUBLE, getPursuit(parameters));
    }
    @Override
    protected void loadParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        if (container.has(targetTypeKey, PersistentDataType.STRING)) {
            parameters.put("targetType", container.get(targetTypeKey, PersistentDataType.STRING));
        }
        if (container.has(rangeKey, PersistentDataType.DOUBLE)) {
            parameters.put("range", container.get(rangeKey, PersistentDataType.DOUBLE));
        }
        if (container.has(pursuitKey, PersistentDataType.DOUBLE)) {
            parameters.put("pursuit", container.get(pursuitKey, PersistentDataType.DOUBLE));
        }
    }
    @Override
    public boolean handleShot(Player player, Arrow arrow, ItemStack bowItem) {
        if (!isBowType(bowItem)) {
            return false;
        }
        Map<String, Object> parameters = getParameters(bowItem);
        String targetType = getTargetType(parameters);
        double range = getRange(parameters);
        double pursuit = getPursuit(parameters);
        UUID playerId = player.getUniqueId();
        int ringCount = plugin.getBowChargingListener().getVisibleRingCount(playerId);
        plugin.getLogger().info("Player " + player.getName() + " shot Yoimiya bow with " + ringCount + " rings visible");
        boolean isFullyCharged = (ringCount >= 4);
        if (isFullyCharged) {
            arrow.getPersistentDataContainer().set(bowTypeKey, PersistentDataType.STRING, getType());
            arrow.setFireTicks(300); 
            YoimiyaHomingArrow homingArrow = new YoimiyaHomingArrow(plugin, arrow, player, targetType, range, pursuit);
            YoimiyaArrow effectsTracker = new YoimiyaArrow(
                    plugin, arrow, player, targetType, range, pursuit, true, true);
            player.sendMessage(ChatColor.GOLD + "Full charge! " + ChatColor.RED + "Main arrow is now fiery and tracking!");
        } else {
            plugin.getLogger().info("Main arrow is a regular arrow (not fully charged)");
        }
        if (ringCount <= 0) {
            plugin.getLogger().info("No rings visible - not spawning extra arrows");
            return true;
        }
        spawnRingArrows(player, arrow, ringCount, targetType, range, pursuit);
        player.sendMessage(ChatColor.GOLD + "You shot " + (ringCount + 1) + " arrows!");
        return true;
    }
    private void spawnRingArrows(Player player, Arrow originalArrow, int ringCount,
                                 String targetType, double range, double pursuit) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Vector perpendicular1 = getPerpendicular(direction); 
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize(); 
        double distance = 2.0;
        double offsetDistance = 1.0;
        Location baseCenter = eyeLocation.clone().add(direction.clone().multiply(distance));
        plugin.getLogger().info("Spawning " + ringCount + " additional arrows from rings");
        Vector originalVelocity = originalArrow.getVelocity();
        double reducedSpeed = originalVelocity.length() * 0.8; 
        for (int ringType = 0; ringType < ringCount; ringType++) {
            Location ringCenter = baseCenter.clone();
            String ringPosition = "";
            switch (ringType) {
                case 0: 
                    ringCenter.add(perpendicular2.clone().multiply(offsetDistance));
                    ringPosition = "top";
                    break;
                case 1: 
                    ringCenter.add(perpendicular1.clone().multiply(offsetDistance));
                    ringPosition = "right";
                    break;
                case 2: 
                    ringCenter.add(perpendicular2.clone().multiply(-offsetDistance));
                    ringPosition = "bottom";
                    break;
                case 3: 
                    ringCenter.add(perpendicular1.clone().multiply(-offsetDistance));
                    ringPosition = "left";
                    break;
            }
            plugin.getLogger().info("Creating arrow from " + ringPosition + " ring");
            player.getWorld().spawnParticle(
                    Particle.LAVA,
                    ringCenter,
                    5,  
                    0.1, 0.1, 0.1,  
                    0.1  
            );
            Vector ringArrowVelocity = originalVelocity.clone().normalize().multiply(reducedSpeed);
            Arrow ringArrow = player.getWorld().spawnArrow(
                    ringCenter,
                    ringArrowVelocity,
                    (float) reducedSpeed,
                    0  
            );
            ringArrow.setShooter(player);
            ringArrow.setCritical(originalArrow.isCritical());
            ringArrow.setDamage(originalArrow.getDamage() * 0.8); 
            ringArrow.setPierceLevel(originalArrow.getPierceLevel());
            ringArrow.setKnockbackStrength(originalArrow.getKnockbackStrength());
            ringArrow.getPersistentDataContainer().set(bowTypeKey, PersistentDataType.STRING, getType());
            YoimiyaHomingArrow homingArrow = new YoimiyaHomingArrow(plugin, ringArrow, player, targetType, range, pursuit);
            YoimiyaArrow effectsTracker = new YoimiyaArrow(plugin, ringArrow, player, targetType, range, pursuit, false, false);
        }
    }
    private Vector getPerpendicular(Vector vector) {
        if (Math.abs(vector.getY()) < 0.9) {
            return new Vector(0, 1, 0).crossProduct(vector).normalize();
        } else {
            return new Vector(1, 0, 0).crossProduct(vector).normalize();
        }
    }
    private String getTargetType(Map<String, Object> parameters) {
        Object value = parameters.get("targetType");
        if (value instanceof String) {
            String type = (String) value;
            return (type.equalsIgnoreCase("DEBUG") || type.equalsIgnoreCase("PLAYER"))
                    ? type.toUpperCase()
                    : "PLAYER"; 
        }
        return "PLAYER"; 
    }
    private double getRange(Map<String, Object> parameters) {
        Object value = parameters.get("range");
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 10.0; 
            }
        }
        return 10.0; 
    }
    private double getPursuit(Map<String, Object> parameters) {
        Object value = parameters.get("pursuit");
        if (value instanceof Double) {
            return validatePursuit((Double) value);
        } else if (value instanceof Number) {
            return validatePursuit(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return validatePursuit(Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
                return 0.5; 
            }
        }
        return 0.5; 
    }
    private double validatePursuit(double value) {
        return Math.max(0.1, Math.min(1.0, value));
    }
}
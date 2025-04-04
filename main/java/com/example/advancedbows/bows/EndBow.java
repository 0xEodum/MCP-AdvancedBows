package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.EndArrow;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;
import java.util.Map;
public class EndBow extends AbstractBow {
    private final NamespacedKey targetTypeKey;
    private final NamespacedKey rangeKey;
    private final NamespacedKey teleportRadiusKey;
    private final NamespacedKey teleportHeightKey;
    public EndBow(AdvancedBows plugin) {
        super(plugin);
        this.targetTypeKey = new NamespacedKey(plugin, "end_target_type");
        this.rangeKey = new NamespacedKey(plugin, "end_range");
        this.teleportRadiusKey = new NamespacedKey(plugin, "end_teleport_radius");
        this.teleportHeightKey = new NamespacedKey(plugin, "end_teleport_height");
    }
    @Override
    public String getType() {
        return "END";
    }
    @Override
    protected String getDisplayName() {
        return "End Bow";
    }
    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        String targetType = getTargetType(parameters);
        double range = getRange(parameters);
        double teleportRadius = getTeleportRadius(parameters);
        double teleportHeight = getTeleportHeight(parameters);
        lore.add(ChatColor.GRAY + "Target: " + ChatColor.WHITE + targetType);
        lore.add(ChatColor.GRAY + "Range: " + ChatColor.WHITE + range + " blocks");
        lore.add(ChatColor.GRAY + "Teleport Radius: " + ChatColor.WHITE + teleportRadius + " blocks");
        lore.add(ChatColor.GRAY + "Teleport Height: " + ChatColor.WHITE + teleportHeight + " blocks");
        lore.add(ChatColor.DARK_PURPLE + "Arrow ignores gravity and teleports above targets!");
    }
    @Override
    protected void storeParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        container.set(targetTypeKey, PersistentDataType.STRING, getTargetType(parameters));
        container.set(rangeKey, PersistentDataType.DOUBLE, getRange(parameters));
        container.set(teleportRadiusKey, PersistentDataType.DOUBLE, getTeleportRadius(parameters));
        container.set(teleportHeightKey, PersistentDataType.DOUBLE, getTeleportHeight(parameters));
    }
    @Override
    protected void loadParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        if (container.has(targetTypeKey, PersistentDataType.STRING)) {
            parameters.put("targetType", container.get(targetTypeKey, PersistentDataType.STRING));
        }
        if (container.has(rangeKey, PersistentDataType.DOUBLE)) {
            parameters.put("range", container.get(rangeKey, PersistentDataType.DOUBLE));
        }
        if (container.has(teleportRadiusKey, PersistentDataType.DOUBLE)) {
            parameters.put("teleportRadius", container.get(teleportRadiusKey, PersistentDataType.DOUBLE));
        }
        if (container.has(teleportHeightKey, PersistentDataType.DOUBLE)) {
            parameters.put("teleportHeight", container.get(teleportHeightKey, PersistentDataType.DOUBLE));
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
        double teleportRadius = getTeleportRadius(parameters);
        double teleportHeight = getTeleportHeight(parameters);
        arrow.setGravity(false);
        arrow.getPersistentDataContainer().set(bowTypeKey, PersistentDataType.STRING, getType());
        new EndArrow(plugin, arrow, player, targetType, range, teleportRadius, teleportHeight);
        return true;
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
    private double getTeleportRadius(Map<String, Object> parameters) {
        Object value = parameters.get("teleportRadius");
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 3.0; 
            }
        }
        return 3.0; 
    }
    private double getTeleportHeight(Map<String, Object> parameters) {
        Object value = parameters.get("teleportHeight");
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 5.0; 
            }
        }
        return 5.0; 
    }
}
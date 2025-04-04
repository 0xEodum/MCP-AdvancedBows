package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.HomingArrow;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;
import java.util.Map;
public class HomingBow extends AbstractBow {
    private final NamespacedKey targetTypeKey;
    private final NamespacedKey rangeKey;
    private final NamespacedKey pursuitKey;
    public HomingBow(AdvancedBows plugin) {
        super(plugin);
        this.targetTypeKey = new NamespacedKey(plugin, "homing_target_type");
        this.rangeKey = new NamespacedKey(plugin, "homing_range");
        this.pursuitKey = new NamespacedKey(plugin, "homing_pursuit");
    }
    @Override
    public String getType() {
        return "HOMING";
    }
    @Override
    protected String getDisplayName() {
        return "Homing Bow";
    }
    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        String targetType = getTargetType(parameters);
        double range = getRange(parameters);
        double pursuit = getPursuit(parameters);
        lore.add(ChatColor.GRAY + "Target: " + ChatColor.WHITE + targetType);
        lore.add(ChatColor.GRAY + "Range: " + ChatColor.WHITE + range + " blocks");
        lore.add(ChatColor.GRAY + "Pursuit: " + ChatColor.WHITE + pursuit);
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
        arrow.getPersistentDataContainer().set(bowTypeKey, PersistentDataType.STRING, getType());
        new HomingArrow(plugin, arrow, player, targetType, range, pursuit);
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
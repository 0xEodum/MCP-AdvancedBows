package com.example.advancedbows.bows;

import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.CursedArrow;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CursedBow extends AbstractBow {
    private final NamespacedKey targetTypeKey;
    private final NamespacedKey rangeKey;
    private final NamespacedKey chargeTimeKey;
    private final NamespacedKey damagePercentKey;
    private final NamespacedKey effectDurationKey;

    public CursedBow(AdvancedBows plugin) {
        super(plugin);
        this.targetTypeKey = new NamespacedKey(plugin, "cursed_target_type");
        this.rangeKey = new NamespacedKey(plugin, "cursed_range");
        this.chargeTimeKey = new NamespacedKey(plugin, "cursed_charge_time");
        this.damagePercentKey = new NamespacedKey(plugin, "cursed_damage_percent");
        this.effectDurationKey = new NamespacedKey(plugin, "cursed_effect_duration");
    }

    @Override
    public String getType() {
        return "CURSED";
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.DARK_PURPLE + "Cursed Bow";
    }

    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        String targetType = getTargetType(parameters);
        double range = getRange(parameters);
        double chargeTime = getChargeTime(parameters);
        double damagePercent = getDamagePercent(parameters);
        int effectDuration = getEffectDuration(parameters);

        lore.add(ChatColor.GRAY + "Target: " + ChatColor.WHITE + targetType);
        lore.add(ChatColor.GRAY + "Range: " + ChatColor.WHITE + range + " blocks");
        lore.add(ChatColor.GRAY + "Charge Time: " + ChatColor.WHITE + chargeTime + " seconds");
        lore.add(ChatColor.GRAY + "Damage: " + ChatColor.WHITE + damagePercent + "% of max health");
        lore.add(ChatColor.GRAY + "Effect Duration: " + ChatColor.WHITE + effectDuration + " seconds");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "» " + ChatColor.LIGHT_PURPLE + "Creates 8 cursed nodes while charging");
        lore.add(ChatColor.DARK_PURPLE + "» " + ChatColor.LIGHT_PURPLE + "Releases homing curse projectiles");
        lore.add(ChatColor.DARK_PURPLE + "» " + ChatColor.LIGHT_PURPLE + "Each projectile applies a unique debuff");
    }

    @Override
    protected void storeParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        container.set(targetTypeKey, PersistentDataType.STRING, getTargetType(parameters));
        container.set(rangeKey, PersistentDataType.DOUBLE, getRange(parameters));
        container.set(chargeTimeKey, PersistentDataType.DOUBLE, getChargeTime(parameters));
        container.set(damagePercentKey, PersistentDataType.DOUBLE, getDamagePercent(parameters));
        container.set(effectDurationKey, PersistentDataType.INTEGER, getEffectDuration(parameters));
    }

    @Override
    protected void loadParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        if (container.has(targetTypeKey, PersistentDataType.STRING)) {
            parameters.put("targetType", container.get(targetTypeKey, PersistentDataType.STRING));
        }
        if (container.has(rangeKey, PersistentDataType.DOUBLE)) {
            parameters.put("range", container.get(rangeKey, PersistentDataType.DOUBLE));
        }
        if (container.has(chargeTimeKey, PersistentDataType.DOUBLE)) {
            parameters.put("chargeTime", container.get(chargeTimeKey, PersistentDataType.DOUBLE));
        }
        if (container.has(damagePercentKey, PersistentDataType.DOUBLE)) {
            parameters.put("damagePercent", container.get(damagePercentKey, PersistentDataType.DOUBLE));
        }
        if (container.has(effectDurationKey, PersistentDataType.INTEGER)) {
            parameters.put("effectDuration", container.get(effectDurationKey, PersistentDataType.INTEGER));
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
        double chargeTime = getChargeTime(parameters);
        double damagePercent = getDamagePercent(parameters);
        int effectDuration = getEffectDuration(parameters);

        UUID playerId = player.getUniqueId();
        int nodeCount = plugin.getBowChargingListener().getVisibleRingCount(playerId);
        boolean isFullyCharged = plugin.getBowChargingListener().isFullyCharged(playerId);

        arrow.remove();

        if (nodeCount <= 0) {
            player.sendMessage(ChatColor.GRAY + "The bow wasn't charged enough!");
            return true;
        }

        int projectilesToSpawn = Math.min(nodeCount, 8);
        new CursedArrow(plugin, player, targetType, range, damagePercent, effectDuration, projectilesToSpawn, isFullyCharged);

        player.sendMessage(ChatColor.DARK_PURPLE + "Released " + projectilesToSpawn + " cursed projectiles!");
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

    private double getChargeTime(Map<String, Object> parameters) {
        Object value = parameters.get("chargeTime");
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

    private double getDamagePercent(Map<String, Object> parameters) {
        Object value = parameters.get("damagePercent");
        if (value instanceof Double) {
            return validateDamagePercent((Double) value);
        } else if (value instanceof Number) {
            return validateDamagePercent(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return validateDamagePercent(Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
                return 5.0;
            }
        }
        return 5.0;
    }

    private double validateDamagePercent(double value) {
        return Math.max(1.0, Math.min(20.0, value));
    }

    private int getEffectDuration(Map<String, Object> parameters) {
        Object value = parameters.get("effectDuration");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 5;
            }
        }
        return 5;
    }
}
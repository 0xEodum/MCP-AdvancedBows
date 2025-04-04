package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.SoulArrow;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class SoulBow extends AbstractBow {
    private final NamespacedKey targetTypeKey;
    private final NamespacedKey rangeKey;
    private final NamespacedKey beamLengthKey;
    private final NamespacedKey fangRadiusKey;
    private final NamespacedKey fangCountKey;
    public SoulBow(AdvancedBows plugin) {
        super(plugin);
        this.targetTypeKey = new NamespacedKey(plugin, "soul_target_type");
        this.rangeKey = new NamespacedKey(plugin, "soul_range");
        this.beamLengthKey = new NamespacedKey(plugin, "soul_beam_length");
        this.fangRadiusKey = new NamespacedKey(plugin, "soul_fang_radius");
        this.fangCountKey = new NamespacedKey(plugin, "soul_fang_count");
    }
    @Override
    public String getType() {
        return "SOUL";
    }
    @Override
    protected String getDisplayName() {
        return ChatColor.DARK_PURPLE + "Soul Bow";
    }
    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        String targetType = getTargetType(parameters);
        double range = getRange(parameters);
        double beamLength = getBeamLength(parameters);
        double fangRadius = getFangRadius(parameters);
        int fangCount = getFangCount(parameters);
        lore.add(ChatColor.GRAY + "Target: " + ChatColor.WHITE + targetType);
        lore.add(ChatColor.GRAY + "Glow Range: " + ChatColor.WHITE + range + " blocks");
        lore.add(ChatColor.GRAY + "Soul Beam Length: " + ChatColor.WHITE + beamLength + " blocks");
        lore.add(ChatColor.GRAY + "Fang Radius: " + ChatColor.WHITE + fangRadius + " blocks");
        lore.add(ChatColor.GRAY + "Fang Count: " + ChatColor.WHITE + fangCount);
        lore.add("");
        lore.add(ChatColor.DARK_BLUE + "» " + ChatColor.AQUA + "Creates a soul charge while aiming");
        lore.add(ChatColor.DARK_BLUE + "» " + ChatColor.AQUA + "Releases a soul beam that steals life");
    }
    @Override
    protected void storeParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        container.set(targetTypeKey, PersistentDataType.STRING, getTargetType(parameters));
        container.set(rangeKey, PersistentDataType.DOUBLE, getRange(parameters));
        container.set(beamLengthKey, PersistentDataType.DOUBLE, getBeamLength(parameters));
        container.set(fangRadiusKey, PersistentDataType.DOUBLE, getFangRadius(parameters));
        container.set(fangCountKey, PersistentDataType.INTEGER, getFangCount(parameters));
    }
    @Override
    protected void loadParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        if (container.has(targetTypeKey, PersistentDataType.STRING)) {
            parameters.put("targetType", container.get(targetTypeKey, PersistentDataType.STRING));
        }
        if (container.has(rangeKey, PersistentDataType.DOUBLE)) {
            parameters.put("range", container.get(rangeKey, PersistentDataType.DOUBLE));
        }
        if (container.has(beamLengthKey, PersistentDataType.DOUBLE)) {
            parameters.put("beamLength", container.get(beamLengthKey, PersistentDataType.DOUBLE));
        }
        if (container.has(fangRadiusKey, PersistentDataType.DOUBLE)) {
            parameters.put("fangRadius", container.get(fangRadiusKey, PersistentDataType.DOUBLE));
        }
        if (container.has(fangCountKey, PersistentDataType.INTEGER)) {
            parameters.put("fangCount", container.get(fangCountKey, PersistentDataType.INTEGER));
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
        double beamLength = getBeamLength(parameters);
        double fangRadius = getFangRadius(parameters);
        int fangCount = getFangCount(parameters);
        UUID playerId = player.getUniqueId();
        boolean isFullyCharged = plugin.getBowChargingListener().isFullyCharged(playerId);
        if (isFullyCharged) {
            arrow.remove();
            player.getWorld().playSound(player.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 0.5f);
            new SoulArrow(plugin, player, targetType, beamLength, fangRadius, fangCount);
            player.sendMessage(ChatColor.DARK_PURPLE + "Soul beam released!");
            return true;
        } else {
            arrow.remove();
            player.sendMessage(ChatColor.GRAY + "The bow wasn't fully charged!");
            return true;
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
    private double getBeamLength(Map<String, Object> parameters) {
        Object value = parameters.get("beamLength");
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 20.0; 
            }
        }
        return 20.0; 
    }
    private double getFangRadius(Map<String, Object> parameters) {
        Object value = parameters.get("fangRadius");
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
    private int getFangCount(Map<String, Object> parameters) {
        Object value = parameters.get("fangCount");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 8; 
            }
        }
        return 8; 
    }
}
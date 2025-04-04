package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.ExplosionArrow;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class ExplosionBow extends AbstractBow {
    private final NamespacedKey chargeTimeKey;
    private final NamespacedKey explosionPowerKey;
    public ExplosionBow(AdvancedBows plugin) {
        super(plugin);
        this.chargeTimeKey = new NamespacedKey(plugin, "explosion_charge_time");
        this.explosionPowerKey = new NamespacedKey(plugin, "explosion_power");
    }
    @Override
    public String getType() {
        return "EXPLOSION";
    }
    @Override
    protected String getDisplayName() {
        return ChatColor.RED + "Explosion Bow";
    }
    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        double chargeTime = getChargeTime(parameters);
        double explosionPower = getExplosionPower(parameters);
        lore.add(ChatColor.GRAY + "Charge Time: " + ChatColor.WHITE + chargeTime + " seconds");
        lore.add(ChatColor.GRAY + "Explosion Power: " + ChatColor.WHITE + explosionPower);
        lore.add("");
        lore.add(ChatColor.GOLD + "» " + ChatColor.RED + "Creates a charging circle while aiming");
        lore.add(ChatColor.GOLD + "» " + ChatColor.RED + "Fully charged arrows explode on impact");
    }
    @Override
    protected void storeParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        container.set(chargeTimeKey, PersistentDataType.DOUBLE, getChargeTime(parameters));
        container.set(explosionPowerKey, PersistentDataType.DOUBLE, getExplosionPower(parameters));
    }
    @Override
    protected void loadParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        if (container.has(chargeTimeKey, PersistentDataType.DOUBLE)) {
            parameters.put("chargeTime", container.get(chargeTimeKey, PersistentDataType.DOUBLE));
        }
        if (container.has(explosionPowerKey, PersistentDataType.DOUBLE)) {
            parameters.put("explosionPower", container.get(explosionPowerKey, PersistentDataType.DOUBLE));
        }
    }
    @Override
    public boolean handleShot(Player player, Arrow arrow, ItemStack bowItem) {
        if (!isBowType(bowItem)) {
            return false;
        }
        Map<String, Object> parameters = getParameters(bowItem);
        double chargeTime = getChargeTime(parameters);
        double explosionPower = getExplosionPower(parameters);
        arrow.getPersistentDataContainer().set(bowTypeKey, PersistentDataType.STRING, getType());
        UUID playerId = player.getUniqueId();
        boolean isFullyCharged = plugin.getBowChargingListener().isFullyCharged(playerId);
        if (isFullyCharged) {
            arrow.setGravity(false);
            Vector direction = player.getEyeLocation().getDirection();
            double speed = arrow.getVelocity().length(); 
            arrow.setVelocity(direction.normalize().multiply(speed));
            new ExplosionArrow(plugin, arrow, player, explosionPower, isFullyCharged);
            player.sendMessage(ChatColor.RED + "Full charge! " + ChatColor.GOLD + "Arrow will explode on impact!");
        } else {
            new ExplosionArrow(plugin, arrow, player, explosionPower, false);
        }
        return true;
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
    private double getExplosionPower(Map<String, Object> parameters) {
        Object value = parameters.get("explosionPower");
        if (value instanceof Double) {
            return validateExplosionPower((Double) value);
        } else if (value instanceof Number) {
            return validateExplosionPower(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return validateExplosionPower(Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
                return 2.0; 
            }
        }
        return 2.0; 
    }
    private double validateExplosionPower(double value) {
        return Math.max(1.0, Math.min(4.0, value));
    }
}
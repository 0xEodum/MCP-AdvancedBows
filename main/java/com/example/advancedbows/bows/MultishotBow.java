package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.MultishotArrow;
import com.example.advancedbows.utils.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import java.util.List;
import java.util.Map;
public class MultishotBow extends AbstractBow {
    private final NamespacedKey arrowCountKey;
    private final NamespacedKey maxDeviationKey;
    public MultishotBow(AdvancedBows plugin) {
        super(plugin);
        this.arrowCountKey = new NamespacedKey(plugin, "multishot_arrow_count");
        this.maxDeviationKey = new NamespacedKey(plugin, "multishot_max_deviation");
    }
    @Override
    public String getType() {
        return "MULTISHOT";
    }
    @Override
    protected String getDisplayName() {
        return "Multishot Bow";
    }
    @Override
    protected void addParametersToLore(List<String> lore, Map<String, Object> parameters) {
        int arrowCount = getArrowCount(parameters);
        double maxDeviation = getMaxDeviation(parameters);
        lore.add(ChatColor.GRAY + "Fires " + ChatColor.WHITE + arrowCount + ChatColor.GRAY + " additional arrows");
        lore.add(ChatColor.GRAY + "Max Deviation: " + ChatColor.WHITE + maxDeviation + "°");
    }
    @Override
    protected void storeParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        container.set(arrowCountKey, PersistentDataType.INTEGER, getArrowCount(parameters));
        container.set(maxDeviationKey, PersistentDataType.DOUBLE, getMaxDeviation(parameters));
    }
    @Override
    protected void loadParameters(PersistentDataContainer container, Map<String, Object> parameters) {
        if (container.has(arrowCountKey, PersistentDataType.INTEGER)) {
            parameters.put("arrowCount", container.get(arrowCountKey, PersistentDataType.INTEGER));
        }
        if (container.has(maxDeviationKey, PersistentDataType.DOUBLE)) {
            parameters.put("maxDeviation", container.get(maxDeviationKey, PersistentDataType.DOUBLE));
        }
    }
    @Override
    public boolean handleShot(Player player, Arrow originalArrow, ItemStack bowItem) {
        if (!isBowType(bowItem)) {
            return false;
        }
        Map<String, Object> parameters = getParameters(bowItem);
        int arrowCount = getArrowCount(parameters);
        double maxDeviation = getMaxDeviation(parameters);
        originalArrow.getPersistentDataContainer().set(bowTypeKey, PersistentDataType.STRING, getType());
        for (int i = 0; i < arrowCount; i++) {
            Arrow newArrow = player.getWorld().spawnArrow(
                    originalArrow.getLocation(),
                    calculateDeviatedVector(originalArrow.getVelocity(), maxDeviation),
                    (float) originalArrow.getVelocity().length(),
                    12.0F  
            );
            copyArrowProperties(originalArrow, newArrow);
            new MultishotArrow(plugin, newArrow, originalArrow, maxDeviation);
        }
        return true;
    }
    private Vector calculateDeviatedVector(Vector original, double maxDeviationDegrees) {
        return MathUtils.getRandomDeviation(original, maxDeviationDegrees);
    }
    private void copyArrowProperties(Arrow source, Arrow target) {
        target.setShooter(source.getShooter());
        target.setCritical(source.isCritical());
        target.setKnockbackStrength(source.getKnockbackStrength());
        target.setDamage(source.getDamage());
        target.setFireTicks(source.getFireTicks());
        target.setPierceLevel(source.getPierceLevel());
        target.setVelocity(target.getVelocity().normalize().multiply(source.getVelocity().length()));
        PersistentDataContainer sourceContainer = source.getPersistentDataContainer();
        PersistentDataContainer targetContainer = target.getPersistentDataContainer();
        targetContainer.set(bowTypeKey, PersistentDataType.STRING, getType());
    }
    private int getArrowCount(Map<String, Object> parameters) {
        Object value = parameters.get("arrowCount");
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 3; 
            }
        }
        return 3; 
    }
    private double getMaxDeviation(Map<String, Object> parameters) {
        Object value = parameters.get("maxDeviation");
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
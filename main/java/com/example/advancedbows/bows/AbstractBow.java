package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public abstract class AbstractBow implements SpecialBow {
    protected final AdvancedBows plugin;
    protected final NamespacedKey bowTypeKey;
    public AbstractBow(AdvancedBows plugin) {
        this.plugin = plugin;
        this.bowTypeKey = new NamespacedKey(plugin, "bow_type");
    }
    @Override
    public ItemStack createBowItem(Map<String, Object> parameters) {
        ItemStack bowItem = new ItemStack(Material.BOW);
        ItemMeta meta = bowItem.getItemMeta();
        if (meta == null) {
            return bowItem;
        }
        meta.setDisplayName(ChatColor.GOLD + getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + getType());
        addParametersToLore(lore, parameters);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(bowTypeKey, PersistentDataType.STRING, getType());
        storeParameters(container, parameters);
        bowItem.setItemMeta(meta);
        return bowItem;
    }
    @Override
    public boolean isBowType(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(bowTypeKey, PersistentDataType.STRING)) {
            return false;
        }
        String bowType = container.get(bowTypeKey, PersistentDataType.STRING);
        return getType().equals(bowType);
    }
    @Override
    public Map<String, Object> getParameters(ItemStack item) {
        Map<String, Object> parameters = new HashMap<>();
        if (!isBowType(item)) {
            return parameters;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return parameters;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        loadParameters(container, parameters);
        return parameters;
    }
    protected abstract String getDisplayName();
    protected abstract void addParametersToLore(List<String> lore, Map<String, Object> parameters);
    protected abstract void storeParameters(PersistentDataContainer container, Map<String, Object> parameters);
    protected abstract void loadParameters(PersistentDataContainer container, Map<String, Object> parameters);
}
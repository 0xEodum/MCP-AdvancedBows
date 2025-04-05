package com.example.advancedbows.bows;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
public class BowManager {
    private final AdvancedBows plugin;
    private final Map<String, SpecialBow> registeredBows;
    public BowManager(AdvancedBows plugin) {
        this.plugin = plugin;
        this.registeredBows = new HashMap<>();
        registerDefaultBows();
    }
    private void registerDefaultBows() {
        registerBow(new MultishotBow(plugin));
        registerBow(new HomingBow(plugin));
        registerBow(new EndBow(plugin));
        registerBow(new YoimiyaBow(plugin));
        registerBow(new ExplosionBow(plugin));
        registerBow(new SoulBow(plugin));
        registerBow(new CursedBow(plugin));
    }
    public void registerBow(SpecialBow bow) {
        registeredBows.put(bow.getType().toLowerCase(), bow);
        plugin.getLogger().info("Registered bow type: " + bow.getType());
    }
    public Optional<SpecialBow> getBow(String type) {
        return Optional.ofNullable(registeredBows.get(type.toLowerCase()));
    }
    public ItemStack createBow(String type, Map<String, Object> parameters) {
        Optional<SpecialBow> bow = getBow(type);
        return bow.map(specialBow -> specialBow.createBowItem(parameters)).orElse(null);
    }
    public boolean handleShot(Player player, Arrow arrow, ItemStack bowItem) {
        for (SpecialBow bow : registeredBows.values()) {
            if (bow.isBowType(bowItem) && bow.handleShot(player, arrow, bowItem)) {
                return true;
            }
        }
        return false;
    }
    public Optional<SpecialBow> getBowForItem(ItemStack item) {
        for (SpecialBow bow : registeredBows.values()) {
            if (bow.isBowType(item)) {
                return Optional.of(bow);
            }
        }
        return Optional.empty();
    }
    public Map<String, SpecialBow> getRegisteredBows() {
        return new HashMap<>(registeredBows);
    }
}
package com.example.advancedbows.bows;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
public interface SpecialBow {
    String getType();
    ItemStack createBowItem(Map<String, Object> parameters);
    boolean handleShot(Player player, Arrow arrow, ItemStack bowItem);
    boolean isBowType(ItemStack item);
    Map<String, Object> getParameters(ItemStack item);
}
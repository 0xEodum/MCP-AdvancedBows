package com.example.advancedbows.listeners;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
public class BowShootListener implements Listener {
    private final AdvancedBows plugin;
    public BowShootListener(AdvancedBows plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        if (!(event.getProjectile() instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow) event.getProjectile();
        boolean handled = plugin.getBowManager().handleShot(player, arrow, bow);
        if (handled) {
            plugin.getLogger().info("Special bow shot handled for " + player.getName());
        }
    }
}
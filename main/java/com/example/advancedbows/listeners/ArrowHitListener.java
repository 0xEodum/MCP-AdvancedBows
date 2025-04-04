package com.example.advancedbows.listeners;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.arrows.SpecialArrow;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.Optional;
public class ArrowHitListener implements Listener {
    private final AdvancedBows plugin;
    private final NamespacedKey bowTypeKey;
    public ArrowHitListener(AdvancedBows plugin) {
        this.plugin = plugin;
        this.bowTypeKey = new NamespacedKey(plugin, "bow_type");
    }
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow) event.getEntity();
        PersistentDataContainer container = arrow.getPersistentDataContainer();
        if (container.has(bowTypeKey, PersistentDataType.STRING)) {
            String bowType = container.get(bowTypeKey, PersistentDataType.STRING);
            plugin.getLogger().info("Special arrow hit: " + bowType);
            Optional<SpecialArrow> specialArrow = plugin.getArrowManager().getSpecialArrow(arrow);
            specialArrow.ifPresent(SpecialArrow::onHit);
        }
    }
}
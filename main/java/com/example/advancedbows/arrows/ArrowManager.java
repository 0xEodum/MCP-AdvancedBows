package com.example.advancedbows.arrows;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.entity.Arrow;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
public class ArrowManager {
    private final AdvancedBows plugin;
    private final Map<Arrow, SpecialArrow> trackedArrows;
    public ArrowManager(AdvancedBows plugin) {
        this.plugin = plugin;
        this.trackedArrows = new HashMap<>();
    }
    public void registerArrow(SpecialArrow specialArrow) {
        trackedArrows.put(specialArrow.getArrow(), specialArrow);
    }
    public void unregisterArrow(Arrow arrow) {
        trackedArrows.remove(arrow);
    }
    public Optional<SpecialArrow> getSpecialArrow(Arrow arrow) {
        return Optional.ofNullable(trackedArrows.get(arrow));
    }
    public void handleArrowHit(Arrow arrow) {
        Optional<SpecialArrow> specialArrow = getSpecialArrow(arrow);
        specialArrow.ifPresent(SpecialArrow::onHit);
    }
    public void cleanUp() {
        for (SpecialArrow arrow : trackedArrows.values()) {
            arrow.remove();
        }
        trackedArrows.clear();
    }
    public boolean isTrackedArrow(Arrow arrow) {
        return trackedArrows.containsKey(arrow);
    }
}
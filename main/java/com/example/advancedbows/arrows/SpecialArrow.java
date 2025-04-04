package com.example.advancedbows.arrows;
import org.bukkit.entity.Arrow;
public interface SpecialArrow {
    Arrow getArrow();
    String getType();
    void onHit();
    void remove();
}
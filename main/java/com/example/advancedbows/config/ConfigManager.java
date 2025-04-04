package com.example.advancedbows.config;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.configuration.file.FileConfiguration;
public class ConfigManager {
    private final AdvancedBows plugin;
    private FileConfiguration config;
    public ConfigManager(AdvancedBows plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    public int getDefaultMultishotArrowCount() {
        return config.getInt("bows.multishot.default_arrow_count", 3);
    }
    public double getDefaultMultishotMaxDeviation() {
        return config.getDouble("bows.multishot.default_max_deviation", 5.0);
    }
    public String getDefaultHomingTargetType() {
        return config.getString("bows.homing.default_target_type", "PLAYER");
    }
    public double getDefaultHomingRange() {
        return config.getDouble("bows.homing.default_range", 10.0);
    }
    public double getDefaultHomingPursuit() {
        return config.getDouble("bows.homing.default_pursuit", 0.5);
    }
    public String getDefaultEndTargetType() {
        return config.getString("bows.end.default_target_type", "PLAYER");
    }
    public double getDefaultEndRange() {
        return config.getDouble("bows.end.default_range", 10.0);
    }
    public double getDefaultEndTeleportRadius() {
        return config.getDouble("bows.end.default_teleport_radius", 3.0);
    }
    public double getDefaultEndTeleportHeight() {
        return config.getDouble("bows.end.default_teleport_height", 5.0);
    }
    public double getDefaultEndTeleportThreshold() {
        return config.getDouble("bows.end.default_teleport_threshold", 3.0);
    }
    public String getDefaultYoimiyaTargetType() {
        return config.getString("bows.yoimiya.default_target_type", "PLAYER");
    }
    public double getDefaultYoimiyaRange() {
        return config.getDouble("bows.yoimiya.default_range", 10.0);
    }
    public double getDefaultYoimiyaPursuit() {
        return config.getDouble("bows.yoimiya.default_pursuit", 0.7);
    }
    public double getDefaultExplosionChargeTime() {
        return config.getDouble("bows.explosion.default_charge_time", 3.0);
    }
    public double getDefaultExplosionPower() {
        return config.getDouble("bows.explosion.default_explosion_power", 2.0);
    }
    public String getDefaultSoulTargetType() {
        return config.getString("bows.soul.default_target_type", "PLAYER");
    }
    public double getDefaultSoulRange() {
        return config.getDouble("bows.soul.default_range", 8.0);
    }
    public double getDefaultSoulBeamLength() {
        return config.getDouble("bows.soul.default_beam_length", 20.0);
    }
    public double getDefaultSoulFangRadius() {
        return config.getDouble("bows.soul.default_fang_radius", 3.0);
    }
    public int getDefaultSoulFangCount() {
        return config.getInt("bows.soul.default_fang_count", 8);
    }
}
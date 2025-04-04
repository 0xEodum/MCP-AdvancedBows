package com.example.advancedbows;
import com.example.advancedbows.arrows.ArrowManager;
import com.example.advancedbows.bows.BowManager;
import com.example.advancedbows.commands.CommandManager;
import com.example.advancedbows.config.ConfigManager;
import com.example.advancedbows.effects.ParticleManager;
import com.example.advancedbows.listeners.ArrowHitListener;
import com.example.advancedbows.listeners.BowChargingListener;
import com.example.advancedbows.listeners.BowShootListener;
import org.bukkit.plugin.java.JavaPlugin;
public class AdvancedBows extends JavaPlugin {
    private static AdvancedBows instance;
    private ConfigManager configManager;
    private BowManager bowManager;
    private ArrowManager arrowManager;
    private ParticleManager particleManager;
    private CommandManager commandManager;
    private BowChargingListener bowChargingListener;
    private final java.util.Map<String, org.bukkit.NamespacedKey> namespacedKeys = new java.util.HashMap<>();
    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        particleManager = new ParticleManager(this);
        arrowManager = new ArrowManager(this);
        bowManager = new BowManager(this);
        commandManager = new CommandManager(this);
        bowChargingListener = new BowChargingListener(this);
        getServer().getPluginManager().registerEvents(new BowShootListener(this), this);
        getServer().getPluginManager().registerEvents(new ArrowHitListener(this), this);
        getServer().getPluginManager().registerEvents(bowChargingListener, this);
        commandManager.registerCommands();
        getLogger().info("AdvancedBows plugin has been enabled!");
    }
    @Override
    public void onDisable() {
        if (arrowManager != null) {
            arrowManager.cleanUp();
        }
        if (particleManager != null) {
            particleManager.cleanUp();
        }
        getLogger().info("AdvancedBows plugin has been disabled!");
    }
    public static AdvancedBows getInstance() {
        return instance;
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public BowManager getBowManager() {
        return bowManager;
    }
    public ParticleManager getParticleManager() {
        return particleManager;
    }
    public ArrowManager getArrowManager() {
        return arrowManager;
    }
    public CommandManager getCommandManager() {
        return commandManager;
    }
    public BowChargingListener getBowChargingListener() {
        return bowChargingListener;
    }
    public org.bukkit.NamespacedKey getNamespacedKey(String key) {
        if (!namespacedKeys.containsKey(key)) {
            namespacedKeys.put(key, new org.bukkit.NamespacedKey(this, key));
        }
        return namespacedKeys.get(key);
    }
}
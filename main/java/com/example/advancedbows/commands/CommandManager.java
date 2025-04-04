package com.example.advancedbows.commands;
import com.example.advancedbows.AdvancedBows;
import org.bukkit.command.PluginCommand;
public class CommandManager {
    private final AdvancedBows plugin;
    public CommandManager(AdvancedBows plugin) {
        this.plugin = plugin;
    }
    public void registerCommands() {
        PluginCommand giveBowCommand = plugin.getCommand("givebow");
        if (giveBowCommand != null) {
            giveBowCommand.setExecutor(new GiveBowCommand(plugin));
            giveBowCommand.setTabCompleter(new GiveBowCommand(plugin));
        }
    }
}
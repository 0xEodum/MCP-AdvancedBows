package com.example.advancedbows.commands;
import com.example.advancedbows.AdvancedBows;
import com.example.advancedbows.bows.SpecialBow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.stream.Collectors;
public class GiveBowCommand implements CommandExecutor, TabCompleter {
    private final AdvancedBows plugin;
    public GiveBowCommand(AdvancedBows plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givebow <player> <bow_type> [parameters...]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return false;
        }
        String bowType = args[1].toUpperCase();
        Optional<SpecialBow> bowOpt = plugin.getBowManager().getBow(bowType);
        if (!bowOpt.isPresent()) {
            sender.sendMessage(ChatColor.RED + "Unknown bow type: " + bowType);
            sender.sendMessage(ChatColor.YELLOW + "Available types: " +
                    String.join(", ", plugin.getBowManager().getRegisteredBows().keySet()));
            return false;
        }
        Map<String, Object> parameters = parseParameters(bowType, Arrays.copyOfRange(args, 2, args.length));
        ItemStack bowItem = plugin.getBowManager().createBow(bowType, parameters);
        if (bowItem == null) {
            sender.sendMessage(ChatColor.RED + "Failed to create bow item");
            return false;
        }
        target.getInventory().addItem(bowItem);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a " + bowType + " bow");
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            String partialType = args[1].toLowerCase();
            completions.addAll(plugin.getBowManager().getRegisteredBows().keySet().stream()
                    .filter(type -> type.toLowerCase().startsWith(partialType))
                    .collect(Collectors.toList()));
        } else if (args.length >= 3) {
            String bowType = args[1].toUpperCase();
            if (bowType.equals("MULTISHOT")) {
                if (args.length == 3) {
                    completions.add("arrowCount=5");
                } else if (args.length == 4) {
                    completions.add("maxDeviation=5.0");
                }
            } else if (bowType.equals("HOMING")) {
                if (args.length == 3) {
                    completions.add("targetType=PLAYER");
                    completions.add("targetType=DEBUG");
                } else if (args.length == 4) {
                    completions.add("range=10.0");
                } else if (args.length == 5) {
                    completions.add("pursuit=0.5");
                }
            }
            else if (bowType.equals("END")) {
                if (args.length == 3) {
                    completions.add("targetType=PLAYER");
                    completions.add("targetType=DEBUG");
                } else if (args.length == 4) {
                    completions.add("range=10.0");
                } else if (args.length == 5) {
                    completions.add("teleportRadius=3.0");
                } else if (args.length == 6) {
                    completions.add("teleportHeight=5.0");
                } else if (args.length == 7) {
                    completions.add("teleportThreshold=3.0");
                }
            }
            else if (bowType.equals("YOIMIYA")) {
                if (args.length == 3) {
                    completions.add("targetType=PLAYER");
                    completions.add("targetType=DEBUG");
                } else if (args.length == 4) {
                    completions.add("range=10.0");
                } else if (args.length == 5) {
                    completions.add("pursuit=0.7");
                }
            }
            else if (bowType.equals("EXPLOSION")) {
                if (args.length == 3) {
                    completions.add("chargeTime=3.0");
                } else if (args.length == 4) {
                    completions.add("explosionPower=2.0");
                }
            }
            else if (bowType.equals("SOUL")) {
                if (args.length == 3) {
                    completions.add("targetType=PLAYER");
                    completions.add("targetType=DEBUG");
                } else if (args.length == 4) {
                    completions.add("range=8.0");
                } else if (args.length == 5) {
                    completions.add("beamLength=20.0");
                } else if (args.length == 6) {
                    completions.add("fangRadius=3.0");
                } else if (args.length == 7) {
                    completions.add("fangCount=8");
                }
            }
            else if (bowType.equals("CURSED")) {
                if (args.length == 3) {
                    completions.add("targetType=PLAYER");
                    completions.add("targetType=DEBUG");
                } else if (args.length == 4) {
                    completions.add("range=10.0");
                } else if (args.length == 5) {
                    completions.add("chargeTime=3.0");
                } else if (args.length == 6) {
                    completions.add("damagePercent=5.0");
                } else if (args.length == 7) {
                    completions.add("effectDuration=5");
                }
            }
        }
        return completions;
    }
    private Map<String, Object> parseParameters(String bowType, String[] paramArgs) {
        Map<String, Object> parameters = new HashMap<>();
        if (bowType.equals("MULTISHOT")) {
            parameters.put("arrowCount", plugin.getConfigManager().getDefaultMultishotArrowCount());
            parameters.put("maxDeviation", plugin.getConfigManager().getDefaultMultishotMaxDeviation());
        } else if (bowType.equals("HOMING")) {
            parameters.put("targetType", plugin.getConfigManager().getDefaultHomingTargetType());
            parameters.put("range", plugin.getConfigManager().getDefaultHomingRange());
            parameters.put("pursuit", plugin.getConfigManager().getDefaultHomingPursuit());
        } else if (bowType.equals("END")) {
            parameters.put("targetType", plugin.getConfigManager().getDefaultEndTargetType());
            parameters.put("range", plugin.getConfigManager().getDefaultEndRange());
            parameters.put("teleportRadius", plugin.getConfigManager().getDefaultEndTeleportRadius());
            parameters.put("teleportHeight", plugin.getConfigManager().getDefaultEndTeleportHeight());
            parameters.put("teleportThreshold", plugin.getConfigManager().getDefaultEndTeleportThreshold());
        } else if (bowType.equals("YOIMIYA")) {
            parameters.put("targetType", plugin.getConfigManager().getDefaultYoimiyaTargetType());
            parameters.put("range", plugin.getConfigManager().getDefaultYoimiyaRange());
            parameters.put("pursuit", plugin.getConfigManager().getDefaultYoimiyaPursuit());
        }
        else if (bowType.equals("EXPLOSION")) {
            parameters.put("chargeTime", plugin.getConfigManager().getDefaultExplosionChargeTime());
            parameters.put("explosionPower", plugin.getConfigManager().getDefaultExplosionPower());
        }
        else if (bowType.equals("SOUL")) {
            parameters.put("targetType", plugin.getConfigManager().getDefaultSoulTargetType());
            parameters.put("range", plugin.getConfigManager().getDefaultSoulRange());
            parameters.put("beamLength", plugin.getConfigManager().getDefaultSoulBeamLength());
            parameters.put("fangRadius", plugin.getConfigManager().getDefaultSoulFangRadius());
            parameters.put("fangCount", plugin.getConfigManager().getDefaultSoulFangCount());
        }
        else if (bowType.equals("CURSED")) {
            parameters.put("targetType", plugin.getConfigManager().getDefaultCursedTargetType());
            parameters.put("range", plugin.getConfigManager().getDefaultCursedRange());
            parameters.put("chargeTime", plugin.getConfigManager().getDefaultCursedChargeTime());
            parameters.put("damagePercent", plugin.getConfigManager().getDefaultCursedDamagePercent());
            parameters.put("effectDuration", plugin.getConfigManager().getDefaultCursedEffectDuration());
        }
        for (String param : paramArgs) {
            String[] parts = param.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = parts[0].toLowerCase();
            String value = parts[1];
            if (key.equals("arrowcount")) {
                try {
                    int count = Integer.parseInt(value);
                    parameters.put("arrowCount", Math.min(count, plugin.getConfig().getInt("settings.max_arrow_count", 50)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("maxdeviation")) {
                try {
                    double deviation = Double.parseDouble(value);
                    parameters.put("maxDeviation", Math.min(deviation, plugin.getConfig().getDouble("settings.max_deviation", 45.0)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("targettype")) {
                String type = value.toUpperCase();
                if (type.equals("PLAYER") || type.equals("DEBUG")) {
                    parameters.put("targetType", type);
                }
            } else if (key.equals("range")) {
                try {
                    double range = Double.parseDouble(value);
                    parameters.put("range", Math.max(1.0, range));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("pursuit")) {
                try {
                    double pursuit = Double.parseDouble(value);
                    parameters.put("pursuit", Math.max(0.1, Math.min(1.0, pursuit)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("teleportradius")) {
                try {
                    double radius = Double.parseDouble(value);
                    parameters.put("teleportRadius", Math.max(1.0, radius));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("teleportheight")) {
                try {
                    double height = Double.parseDouble(value);
                    parameters.put("teleportHeight", Math.max(1.0, height));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("teleportthreshold")) {
                try {
                    double threshold = Double.parseDouble(value);
                    parameters.put("teleportThreshold", Math.max(0.5, threshold));
                } catch (NumberFormatException e) {
                }
            }
            else if (key.equals("chargetime")) {
                try {
                    double chargeTime = Double.parseDouble(value);
                    parameters.put("chargeTime", Math.max(0.5, Math.min(10.0, chargeTime)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("explosionpower")) {
                try {
                    double power = Double.parseDouble(value);
                    parameters.put("explosionPower", Math.max(1.0, Math.min(4.0, power)));
                } catch (NumberFormatException e) {
                }
            }
            else if (key.equals("beamlength")) {
                try {
                    double length = Double.parseDouble(value);
                    parameters.put("beamLength", Math.max(5.0, Math.min(50.0, length)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("fangradius")) {
                try {
                    double radius = Double.parseDouble(value);
                    parameters.put("fangRadius", Math.max(1.0, Math.min(5.0, radius)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("fangcount")) {
                try {
                    int count = Integer.parseInt(value);
                    parameters.put("fangCount", Math.max(4, Math.min(16, count)));
                } catch (NumberFormatException e) {
                }
            }
            else if (key.equals("damagepercent")) {
                try {
                    double damagePercent = Double.parseDouble(value);
                    parameters.put("damagePercent", Math.max(1.0, Math.min(20.0, damagePercent)));
                } catch (NumberFormatException e) {
                }
            } else if (key.equals("effectduration")) {
                try {
                    int effectDuration = Integer.parseInt(value);
                    parameters.put("effectDuration", Math.max(1, Math.min(30, effectDuration)));
                } catch (NumberFormatException e) {
                }
            }
        }
        return parameters;
    }
}
package com.sagakenichi.freelifemarine;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MarineCommand implements CommandExecutor, TabCompleter {

    private static final List<String> MOB_NAMES = List.of("shark", "orca", "crab");
    private static final List<String> SHOW_ACTIONS = List.of("start", "stop", "status", "reload", "list");

    private final MarineMobService mobs;
    private final OrcaShowManager shows;

    public MarineCommand(MarineMobService mobs, OrcaShowManager shows) {
        this.mobs = mobs;
        this.shows = shows;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            return handleSpawn(sender, label, args);
        }
        if (args[0].equalsIgnoreCase("show")) {
            return handleShow(sender, label, args);
        }

        sendUsage(sender, label);
        return true;
    }

    private boolean handleSpawn(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("freelifemarine.spawn")) {
            sender.sendMessage("You do not have permission to spawn marine mobs.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be run by a player.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage("Usage: /" + label + " spawn <shark|orca|crab>");
            return true;
        }

        MarineMobType type = MarineMobType.fromInput(args[1]);
        if (type == null) {
            player.sendMessage("Unknown marine mob. Use shark, orca, or crab.");
            return true;
        }

        MarineMobService.MarineMob mob = mobs.spawn(player, type);
        String rideHint = type.rideable()
                ? " Right-click it to ride (" + mob.seatCount() + " seat" + (mob.seatCount() == 1 ? "" : "s")
                    + "). The first rider uses normal mounted movement controls."
                : " It moves on its own and is not rideable.";
        player.sendMessage("Spawned " + type.displayName() + " with " + (int) type.maxHealth() + " health." + rideHint);
        return true;
    }

    private boolean handleShow(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("freelifemarine.show")) {
            sender.sendMessage("You do not have permission to manage orca shows.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /" + label + " show <start [id]|stop|status|reload|list>");
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "start" -> sender.sendMessage(shows.startShow(args.length >= 3 ? args[2] : null));
            case "stop" -> sender.sendMessage(shows.stopShow());
            case "status" -> sender.sendMessage(shows.status());
            case "reload" -> {
                shows.reload();
                sender.sendMessage("Reloaded orca show configuration.");
            }
            case "list" -> {
                List<String> ids = shows.showIds();
                sender.sendMessage(ids.isEmpty() ? "No shows are configured." : "Configured shows: " + String.join(", ", ids));
            }
            default -> sender.sendMessage("Usage: /" + label + " show <start [id]|stop|status|reload|list>");
        }
        return true;
    }

    private static void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " spawn <shark|orca|crab>");
        sender.sendMessage("/" + label + " show <start [id]|stop|status|reload|list>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> roots = new ArrayList<>();
            if (sender.hasPermission("freelifemarine.spawn")) {
                roots.add("spawn");
            }
            if (sender.hasPermission("freelifemarine.show")) {
                roots.add("show");
            }
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return roots.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return MOB_NAMES.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("show")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return SHOW_ACTIONS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("show") && args[1].equalsIgnoreCase("start")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return shows.showIds().stream().filter(value -> value.startsWith(prefix)).toList();
        }
        return List.of();
    }
}

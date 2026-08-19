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
    private static final List<String> SHOW_ACTIONS = List.of(
            "start", "stop", "status", "reload", "list",
            "set-center", "set-facing", "set-time", "add-time", "remove-time", "enable", "disable"
    );

    private final MarineMobService mobs;
    private final MarineFood food;
    private final OrcaShowManager shows;

    public MarineCommand(MarineMobService mobs, MarineFood food, OrcaShowManager shows) {
        this.mobs = mobs;
        this.food = food;
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
        if (args[0].equalsIgnoreCase("food")) {
            return handleFood(sender, label, args);
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

    private boolean handleFood(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("freelifemarine.food")) {
            sender.sendMessage("You do not have permission to receive marine food.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be run by a player.");
            return true;
        }
        if (args.length > 2) {
            player.sendMessage("Usage: /" + label + " food [1-64]");
            return true;
        }
        int amount = 1;
        if (args.length == 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                player.sendMessage("Amount must be a number from 1 to 64.");
                return true;
            }
        }
        if (amount < 1 || amount > 64) {
            player.sendMessage("Amount must be from 1 to 64.");
            return true;
        }
        food.give(player, amount);
        player.sendMessage("§b海の餌 §fx" + amount + " を受け取りました。手に持つか、水中へ投げて使えます。");
        return true;
    }

    private boolean handleShow(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("freelifemarine.show")) {
            sender.sendMessage("You do not have permission to manage orca shows.");
            return true;
        }
        if (args.length < 2) {
            sendShowUsage(sender, label);
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
            case "set-center" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("set-center must be run by a player at the desired pool center.");
                } else {
                    sender.sendMessage(shows.setCenter(player, args.length >= 3 ? args[2] : null));
                }
            }
            case "set-facing" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("set-facing must be run by a player looking toward the show direction.");
                } else {
                    sender.sendMessage(shows.setFacing(player, args.length >= 3 ? args[2] : null));
                }
            }
            case "set-time" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " show set-time <HH:mm> [id]");
                } else {
                    sender.sendMessage(shows.setSingleTime(args.length >= 4 ? args[3] : null, args[2]));
                }
            }
            case "add-time" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " show add-time <HH:mm> [id]");
                } else {
                    sender.sendMessage(shows.addTime(args.length >= 4 ? args[3] : null, args[2]));
                }
            }
            case "remove-time" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /" + label + " show remove-time <HH:mm> [id]");
                } else {
                    sender.sendMessage(shows.removeTime(args.length >= 4 ? args[3] : null, args[2]));
                }
            }
            case "enable" -> sender.sendMessage(shows.setEnabled(args.length >= 3 ? args[2] : null, true));
            case "disable" -> sender.sendMessage(shows.setEnabled(args.length >= 3 ? args[2] : null, false));
            default -> sendShowUsage(sender, label);
        }
        return true;
    }

    private static void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " spawn <shark|orca|crab>");
        sender.sendMessage("/" + label + " food [1-64]");
        sender.sendMessage("/" + label + " show <...>");
    }

    private static void sendShowUsage(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " show start [id] | stop | status | list | reload");
        sender.sendMessage("/" + label + " show set-center [id] | set-facing [id]");
        sender.sendMessage("/" + label + " show set-time <HH:mm> [id]");
        sender.sendMessage("/" + label + " show add-time <HH:mm> [id] | remove-time <HH:mm> [id]");
        sender.sendMessage("/" + label + " show enable [id] | disable [id]");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> roots = new ArrayList<>();
            if (sender.hasPermission("freelifemarine.spawn")) roots.add("spawn");
            if (sender.hasPermission("freelifemarine.food")) roots.add("food");
            if (sender.hasPermission("freelifemarine.show")) roots.add("show");
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return roots.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return MOB_NAMES.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("food")) {
            return List.of("1", "8", "16", "32", "64").stream().filter(value -> value.startsWith(args[1])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("show")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return SHOW_ACTIONS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("show")
                && List.of("start", "set-center", "set-facing", "enable", "disable").contains(args[1].toLowerCase(Locale.ROOT))) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return shows.showIds().stream().filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("show")
                && List.of("set-time", "add-time", "remove-time").contains(args[1].toLowerCase(Locale.ROOT))) {
            return List.of("10:00", "13:00", "15:30").stream().filter(value -> value.startsWith(args[2])).toList();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("show")
                && List.of("set-time", "add-time", "remove-time").contains(args[1].toLowerCase(Locale.ROOT))) {
            String prefix = args[3].toLowerCase(Locale.ROOT);
            return shows.showIds().stream().filter(value -> value.startsWith(prefix)).toList();
        }
        return List.of();
    }
}

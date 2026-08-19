package com.sagakenichi.freelifemarine;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class MarineCommand implements CommandExecutor, TabCompleter {

    private static final List<String> MOB_NAMES = List.of("shark", "orca", "crab");

    private final MarineMobService mobs;

    public MarineCommand(MarineMobService mobs) {
        this.mobs = mobs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be run by a player.");
            return true;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("spawn")) {
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
                ? " Right-click it to ride (" + mob.seatCount() + " seat" + (mob.seatCount() == 1 ? "" : "s") + ")."
                : " It moves on its own and is not rideable.";
        player.sendMessage("Spawned " + type.displayName() + " with " + (int) type.maxHealth() + " health." + rideHint);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return "spawn".startsWith(args[0].toLowerCase(Locale.ROOT)) ? List.of("spawn") : List.of();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return MOB_NAMES.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        return List.of();
    }
}

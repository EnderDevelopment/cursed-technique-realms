package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public final class CursedCommand implements CommandExecutor {
    private final Main plugin;
    private static final Map<String, String> TECHNIQUES = Map.of(
            "limitless", "Limitless",
            "cleave", "Shrine",
            "boogie", "Boogie Woogie"
    );

    public CursedCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players.");
            return true;
        }
        if (!player.hasPermission("cursed.use")) {
            player.sendMessage(Component.text("You do not have permission to use cursed techniques.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(player);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "energy" -> player.sendMessage(Component.text("Cursed energy: " + plugin.getEnergy(player) + "/100", NamedTextColor.LIGHT_PURPLE));
            case "technique" -> technique(player, args);
            case "blackflash" -> plugin.blackFlash(player);
            case "limitless" -> plugin.limitless(player);
            case "cleave" -> plugin.cleave(player);
            case "reverse" -> plugin.reverseCursedTechnique(player);
            case "domain" -> plugin.domain(player);
            case "give" -> give(player, args);
            case "boogie" -> boogie(player);
            default -> player.sendMessage(Component.text("Unknown technique. Use /cursed help.", NamedTextColor.RED));
        }
        return true;
    }

    private void help(Player player) {
        player.sendMessage(Component.text("Cursed Techniques", NamedTextColor.DARK_PURPLE));
        player.sendMessage(Component.text("/cursed technique <limitless|cleave|boogie>", NamedTextColor.LIGHT_PURPLE));
        player.sendMessage(Component.text("/cursed blackflash  /cursed limitless  /cursed cleave", NamedTextColor.LIGHT_PURPLE));
        player.sendMessage(Component.text("/cursed reverse  /cursed domain  /cursed energy", NamedTextColor.LIGHT_PURPLE));
        player.sendMessage(Component.text("Abilities consume energy and regenerate over time.", NamedTextColor.GRAY));
    }

    private void technique(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Current technique: " + plugin.getTechnique(player), NamedTextColor.LIGHT_PURPLE));
            return;
        }
        String selected = TECHNIQUES.get(args[1].toLowerCase(Locale.ROOT));
        if (selected == null) {
            player.sendMessage(Component.text("Choose limitless, cleave, or boogie.", NamedTextColor.RED));
            return;
        }
        plugin.setTechnique(player, selected);
        player.sendMessage(Component.text("Technique selected: " + selected, NamedTextColor.GOLD));
    }

    private void give(Player player, String[] args) {
        if (!player.hasPermission("cursed.admin")) {
            player.sendMessage(Component.text("Admin permission required.", NamedTextColor.RED));
            return;
        }
        player.getInventory().addItem(plugin.cursedTalisman());
        player.sendMessage(Component.text("A cursed talisman has been given to you.", NamedTextColor.LIGHT_PURPLE));
    }

    private void boogie(Player player) {
        if (!plugin.ready(player, "Boogie Woogie", 6000) || !plugin.spend(player, 15)) return;
        var target = player.getTargetBlockExact(12);
        if (target == null) {
            player.sendMessage(Component.text("Boogie Woogie needs a block to clap toward.", NamedTextColor.GRAY));
            return;
        }
        player.teleport(target.getLocation().add(0.5, 1, 0.5));
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1.5f);
        player.sendMessage(Component.text("Boogie Woogie!", NamedTextColor.YELLOW));
    }
}

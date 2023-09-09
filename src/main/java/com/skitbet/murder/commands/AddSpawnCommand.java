package com.skitbet.murder.commands;

import com.skitbet.murder.MurderPlugin;
import com.skitbet.murder.map.LocalGameMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AddSpawnCommand implements CommandExecutor {

    private final MurderPlugin plugin = MurderPlugin.INSTANCE;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;

        if (!player.hasPermission("murder.addspawn")) {
            return true;
        }

        if (MurderPlugin.INSTANCE.getGameManager().getCurrentMap() == null) {
            player.sendMessage(ChatColor.RED + "You're not in a map dummy.");
            return true;
        }
        String currentMapName = MurderPlugin.INSTANCE.getGameManager().getCurrentMap().getName();

        ConfigurationSection mapsSection = plugin.getConfig().getConfigurationSection("maps");
        if (mapsSection != null) {
            List<String> spawns = mapsSection.getStringList(currentMapName + ".spawns");

            String newLoc = locationToString(player.getLocation());
            if (spawns.contains(newLoc)) {
                player.sendMessage(ChatColor.RED + "Location already set as a spawn.");
                return true;
            }
            spawns.add(newLoc);
            mapsSection.set(currentMapName + ".spawns", spawns);
            plugin.saveConfig();

            player.sendMessage(ChatColor.GREEN + "Added a new spawn point for the map " + currentMapName);
        }




        return false;
    }

    private String locationToString(Location loc) {
        return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }
}

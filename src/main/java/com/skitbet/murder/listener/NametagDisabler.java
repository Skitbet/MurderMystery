package com.skitbet.murder.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class NametagDisabler implements Listener {

//    public static Scoreboard scoreboard;
//    public static Team team;
//
//    public NametagDisabler() {
//        if (scoreboard == null) {
//            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
//            team = scoreboard.registerNewTeam("hide_nametag");
//            team.setNameTagVisibility(NameTagVisibility.NEVER);
//        }
//
//        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
//            enableBoard(onlinePlayer);
//        }
//    }
//
//    @EventHandler
//    public void onPlayerJoin(PlayerJoinEvent event) {
//        enableBoard(event.getPlayer());
//    }
//
//    @EventHandler
//    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
//        enableBoard(e.getPlayer());
//    }
//
//    public void enableBoard(Player player) {
//        team.addPlayer(player);
//        player.setScoreboard(scoreboard);
//    }
//
//    public void removeBoard(Player player) {
//        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
//    }
}
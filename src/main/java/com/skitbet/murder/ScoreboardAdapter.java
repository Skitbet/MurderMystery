package com.skitbet.murder;

import com.skitbet.murder.game.GameManager;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardAdapter implements AssembleAdapter {

    private final GameManager gameManager = MurderPlugin.INSTANCE.getGameManager();

    @Override
    public String getTitle(Player player) {
        return "&bCosmo&6&lVerse";
    }

    @Override
    public List<String> getLines(Player player) {
        final List<String> toReturn = new ArrayList<>();

        toReturn.add("&7---------------------");
        toReturn.add("&r ");

        switch (gameManager.getStatus()) {
            case WAITING -> {
                toReturn.add("&6» &bWaiting...");
            }
            case STARTING -> {
                toReturn.add("&6» &bPreparing game...");
            }
            case PLAYING -> {
                toReturn.add("&6» &bTime Left&7: &3" + gameManager.remainingTime);
                toReturn.add("&6» &bRole&7: &3" + gameManager.getPlayersRole(player));
                if (gameManager.murderer == player) {
                    toReturn.add("&6» &bKills&7: &3" + gameManager.killedPlayers);
                }
            }
            case ENDING -> {
                toReturn.add("&6» &cGame Over...");
                toReturn.add("&6» &bPreparing new game...");
            }
        }

        toReturn.add("&r ");
        toReturn.add("&bcosmoverse.gay");

        return toReturn;
    }
}

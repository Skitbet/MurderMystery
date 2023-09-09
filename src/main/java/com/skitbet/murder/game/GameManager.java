package com.skitbet.murder.game;

import com.skitbet.murder.MurderPlugin;
import com.skitbet.murder.map.GameMap;
import com.skitbet.murder.util.ItemUtil;
import com.skitbet.murder.util.MessageUtils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

@Getter
public class GameManager {
    private int minimumPlayers = 2;
    private int countdownDuration = 5;
    private int gameDuration = 300;

    private MurderPlugin plugin = MurderPlugin.INSTANCE;
    private GameStatus status;
    private boolean murdererActive;
    private List<Player> alivePlayers = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    public Player murderer;
    public Player detective;
    public Player bowHolder;
    public Location detectiveBowLocation;
    public ArmorStand detectiveDeathArmorStand;
    public int killedPlayers;
    public int remainingTime;
    private GameMap currentMap;

    private BukkitRunnable playerCountCheck;
    private BukkitRunnable gameStartCountdown;
    private BukkitRunnable murdererFreeCountdown;
    private BukkitRunnable gameTimer;

    public GameManager() {
        plugin.getServer().getPluginManager().registerEvents(new GameListeners(this), plugin);
        waitForPlayers(true);
    }

    public void resetGameStats() {
        this.murdererActive = false;
        this.bowHolder = null;
        this.detectiveBowLocation = null;
        this.killedPlayers = 0;
        if (playerCountCheck != null && !playerCountCheck.isCancelled()) playerCountCheck.cancel();
        if (gameStartCountdown != null && !gameStartCountdown.isCancelled()) gameStartCountdown.cancel();
        if (murdererFreeCountdown != null && !murdererFreeCountdown.isCancelled()) murdererFreeCountdown.cancel();
        if (gameTimer != null && !gameTimer.isCancelled()) gameTimer.cancel();
    }

    public void waitForPlayers(boolean loadNewMap) {
        status = GameStatus.WAITING;
        Bukkit.getLogger().info("Waiting for players.");
        resetGameStats();

        if (loadNewMap) {
            GameMap lastMap = currentMap;
            currentMap = plugin.getRandomLocalMap();
            currentMap.load();
            Bukkit.getLogger().info("Loading map.");

            Bukkit.getOnlinePlayers().forEach(plr -> {
                plr.sendMessage("§r ");
                plr.sendMessage("§r ");
                plr.sendMessage("§r ");
                plr.sendMessage("§c§lLOADING THE NEXT MAP... PLEASE EXPECT LAG.");
                plr.sendMessage("§r ");
                plr.sendMessage("§r ");
                plr.sendMessage("§r ");
            });

            while (!currentMap.isLoaded()) {
                // wait for map to load.....
            }

            Bukkit.getLogger().info("Map has successfully loaded.");

            for (Player alivePlayer : Bukkit.getOnlinePlayers()) {
                alivePlayer.teleport(currentMap.getRandomSpawn());
                alivePlayer.getInventory().clear();
            }

            if (lastMap != null && lastMap.isLoaded()) {
                Bukkit.getLogger().info("Unloading past map.");
                lastMap.unload();

                while (lastMap.isLoaded()) {
                    // wait for map to load.....
                }

                Bukkit.getLogger().info("Last map has been unloaded.");
            }
        }

         playerCountCheck = new BukkitRunnable() {
            int readyToSendUpdate = 0;

            @Override
            public void run() {
                int currentPlayers = Bukkit.getOnlinePlayers().size();

                if (currentPlayers >= minimumPlayers) {
                    startCountdown();
                    this.cancel();
                    return;
                }

                if (readyToSendUpdate >= 10) {
                    int playersNeeded = (Math.max(0, minimumPlayers - currentPlayers) + 1);

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage("§r ");
                        player.sendMessage("§eWaiting on §c" + playersNeeded + "§e players");
                        player.sendMessage("§r ");
                    });

                    readyToSendUpdate = 0;
                }

                readyToSendUpdate++;
            }
        };

        try {
            playerCountCheck.runTaskTimer(plugin, 0L, 10L);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void startCountdown() {
        status = GameStatus.STARTING;
        Bukkit.getLogger().info("Starting countdown.");

        gameStartCountdown = new BukkitRunnable() {
            int secondsLeft = countdownDuration;

            @Override
            public void run() {
                int currentPlayers = Bukkit.getOnlinePlayers().size();
                if (currentPlayers < minimumPlayers) {
                    waitForPlayers(false);
                    this.cancel();
                } else if (secondsLeft > 0) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage("§r ");
                        player.sendMessage("§bGame starting in §6" + secondsLeft + "§b seconds.");
                        player.sendMessage("§r ");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    });
                    secondsLeft--;
                } else {
                    start();
                    this.cancel();
                }
            }
        };

        try {
            gameStartCountdown.runTaskTimer(plugin, 0L, 20L);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        remainingTime = gameDuration;
        status = GameStatus.PLAYING;
        alivePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        // Select murderer and detective
        Collections.shuffle(alivePlayers);
        murderer = alivePlayers.get(0);
        detective = alivePlayers.get(1);
        bowHolder = detective;

        MessageUtils.sendCenteredMessage(murderer, "&r ");
        MessageUtils.sendCenteredMessage(murderer, "&7-----------------------------------------------");
        MessageUtils.sendCenteredMessage(murderer, "&r ");
        MessageUtils.sendCenteredMessage(murderer, "&aYou are the &c&lMURDERER");
        MessageUtils.sendCenteredMessage(murderer, "&aTry to kill all the players without getting shot!");
        MessageUtils.sendCenteredMessage(murderer, "&r ");
        MessageUtils.sendCenteredMessage(murderer, "&7-----------------------------------------------");
        MessageUtils.sendCenteredMessage(murderer, "&r ");
        murderer.sendTitle("§aYou are the §c§lMURDERER", "", 0, 40, 0);

        MessageUtils.sendCenteredMessage(detective, "&r ");
        MessageUtils.sendCenteredMessage(detective, "&7-----------------------------------------------");
        MessageUtils.sendCenteredMessage(detective, "&r ");
        MessageUtils.sendCenteredMessage(detective, "&aYou are the &9&lDETECTIVE");
        MessageUtils.sendCenteredMessage(detective, "&aFind and kill the murderer!");
        MessageUtils.sendCenteredMessage(detective, "&r ");
        MessageUtils.sendCenteredMessage(detective, "&7-----------------------------------------------");
        MessageUtils.sendCenteredMessage(detective, "&r ");
        detective.sendTitle("§aYou are the §9§lDETECTIVE!", "", 0, 40, 0);

        for (Player alivePlayer : alivePlayers) {
            setPlayerToGame(alivePlayer);
            if (murderer == alivePlayer || detective == alivePlayer) continue;
            MessageUtils.sendCenteredMessage(alivePlayer, "&r ");
            MessageUtils.sendCenteredMessage(alivePlayer, "&7-----------------------------------------------");
            MessageUtils.sendCenteredMessage(alivePlayer, "&r ");
            MessageUtils.sendCenteredMessage(alivePlayer, "&aYour role is &a&lINNOCENT");
            MessageUtils.sendCenteredMessage(alivePlayer, "&aTry to stay alive as long as possible!");
            MessageUtils.sendCenteredMessage(alivePlayer, "&r ");
            MessageUtils.sendCenteredMessage(alivePlayer, "&7-----------------------------------------------");
            MessageUtils.sendCenteredMessage(alivePlayer, "&r ");
            alivePlayer.sendTitle("§aYou are §a§lINNOCENT!", "", 0, 40, 0);

            alivePlayer.teleport(currentMap.getRandomSpawn());
            alivePlayer.getInventory().clear();
        }

        murdererFreeCountdown = new BukkitRunnable() {
            int countdown = 10;
            @Override
            public void run() {
                if (countdown <= 5) {
                    if (countdown == 0) {
                        murderer.getInventory().setItem(0, ItemUtil.getMurderWeapon());
                        detective.getInventory().setItem(0, ItemUtil.getDetectiveBow());
                        detective.getInventory().setItem(10, new ItemStack(Material.ARROW, 1));
                        murdererActive = true;
                        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f));
                        this.cancel();
                        return;
                    }

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage("§r ");
                        player.sendMessage("§eThe murderer will receive a weapon in §c" + countdown + "§e seconds.");
                        player.sendMessage("§r ");
                    });
                }
                countdown--;
            }
        };
        murdererFreeCountdown.runTaskTimer(MurderPlugin.INSTANCE, 0L, 20L);
        startGameTimer();
    }

    public void startGameTimer() {
        remainingTime = gameDuration;
        gameTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime > 0) {
                    remainingTime--;
                } else {
                    end(false, null);
                    this.cancel();
                }
            }
        };
        gameTimer.runTaskTimer(MurderPlugin.INSTANCE, 0L, 20L);
    }

    public void end(boolean murderWin, Player murdererKiller) {
        if (status != GameStatus.PLAYING) return;
        status = GameStatus.ENDING;
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            MessageUtils.sendCenteredMessage(player, "&r ");
            MessageUtils.sendCenteredMessage(player, "&7-----------------------------------------------");
            MessageUtils.sendCenteredMessage(player, "&r ");
            MessageUtils.sendCenteredMessage(player, "&c&lGAME OVER! ");
            MessageUtils.sendCenteredMessage(player, "&r ");
            MessageUtils.sendCenteredMessage(player, "&7Winner: " + (murderWin ? "&c&lMURDERERS" : "&a&lINNOCENTS"));
            MessageUtils.sendCenteredMessage(player, "&r ");
            MessageUtils.sendCenteredMessage(player, "&7Murderer: &c" + murderer.getName());
            MessageUtils.sendCenteredMessage(player, "&7Detective: &9" + (!isDetectiveAlive() ? "&m" : "") + detective.getName());
            MessageUtils.sendCenteredMessage(player, "&r ");
            if (murdererKiller != null) {
                MessageUtils.sendCenteredMessage(player, "&7Hero: &a" + murdererKiller.getName());
                MessageUtils.sendCenteredMessage(player, "&r ");
            }
            MessageUtils.sendCenteredMessage(player, "&7-----------------------------------------------");
            MessageUtils.sendCenteredMessage(player, "&r ");
            player.sendTitle("§c§lGAME OVER!", "", 0, 40, 0);
        });

        alivePlayers.forEach(this::setPlayerToSpec);

        resetGameStats();

        new BukkitRunnable() {
            @Override
            public void run() {
                waitForPlayers(true);
            }
        }.runTaskLater(MurderPlugin.INSTANCE, 60L);
    }



    public void handleDeath(Player victim, boolean killedByMurderer, boolean voidDeath) {
        alivePlayers.remove(victim);
        setPlayerToSpec(victim);

        if (killedByMurderer) killedPlayers++;

        // bow holder died
        if (victim == bowHolder) {
            bowHolder = null;
            Location bowLoc = victim.getLocation();
            if (voidDeath) {
                bowLoc = currentMap.getRandomSpawn();
            }
            detectiveBowLocation = bowLoc;
            detectiveDeathArmorStand = createDeathArmorStand(bowLoc);
        }

        // murderer died
        if (victim == murderer) {
            end(false, null);
            return;
        }

        // if murderer is only one alive they win
        if (isMurdererOnlyAlive()) {
            end(true, null);
            return;
        }


    }

    public boolean isMurdererOnlyAlive() {
        int alivePlayerCount = 0;
        for (Player alivePlayer : alivePlayers) {
            if (alivePlayer != murderer) {
                alivePlayerCount++;
            }
        }
        return alivePlayerCount == 0;
    }

    public boolean isDetectiveAlive() {
        return alivePlayers.contains(detective);
    }

    public int getInnocentsAlive() {
        int alivePlayerCount = 0;
        for (Player alivePlayer : alivePlayers) {
            if (alivePlayer != murderer) {
                alivePlayerCount++;
            }
        }
        return alivePlayerCount;
    }

    public void removePlayer(Player player) {
        alivePlayers.remove(player);
        handleDeath(player, false, false);
    }

    public void setPlayerToSpec(Player player) {
        Bukkit.getOnlinePlayers().forEach(bukkitPlr -> {
            if (!alivePlayers.contains(bukkitPlr)) {
                player.hidePlayer(bukkitPlr);
            }
        });

        player.getInventory().clear();
        player.setSaturation(20);
        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvisible(false);

        spectators.add(player);
    }

    public void setPlayerToGame(Player player) {
        Bukkit.getOnlinePlayers().forEach(bukkitPlr -> {
            if (alivePlayers.contains(bukkitPlr)) {
                player.showPlayer(bukkitPlr);
            }
        });

        player.setSaturation(20);
        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvisible(false);

        spectators.remove(player);
    }

    public ArmorStand createDeathArmorStand(Location location) {
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());

        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setBasePlate(false);
        stand.setVisible(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.BOW, 1));
        return stand;
    }

    public ArmorStand createSpectatorArmorStand(Location location) {
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setBasePlate(false);
        stand.setVisible(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.SKELETON_SKULL, 1));
        return stand;
    }

    public String getPlayersRole(Player player) {
        if (player == detective) return "&b&lDetective";
        if (player == murderer) return "&c&lMurderer";
        return "&a&lInnocent";

    }
}
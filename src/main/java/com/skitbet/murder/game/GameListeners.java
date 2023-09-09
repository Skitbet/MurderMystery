package com.skitbet.murder.game;

import com.skitbet.murder.MurderPlugin;
import com.skitbet.murder.util.ItemUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GameListeners implements Listener {

    private final GameManager gameManager;

    public GameListeners(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the game is currently playing
        if (gameManager.getStatus() != GameStatus.PLAYING) {
            System.out.println("not playing");
            return;
        }

        // Cancel damage events for ArmorStands
        if (event.getEntity() instanceof ArmorStand) {
            System.out.println("armorstand");
            event.setCancelled(true);
            return;
        }

        // Check if the entity is a player
        if (!(event.getEntity() instanceof Player)) {
            System.out.println("not a player");
            return;
        }

        Player victim = (Player) event.getEntity();

        // Handle player damage events
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            event.setCancelled(true);

            if (gameManager.getSpectators().contains(victim)) {
                System.out.println("spectator?");
                return;
            }

            /**
             * Handle murderer killing player with knife
             */
            if (damager.getInventory().getItemInMainHand() == null) return;
            if (damager == gameManager.getMurderer() && gameManager.isMurdererActive() && damager.getInventory().getItemInMainHand().isSimilar(ItemUtil.getMurderWeapon())) {
                gameManager.handleDeath(victim, true, false);
                victim.playSound(victim.getLocation(), Sound.ENTITY_BAT_DEATH, 1.0f, 1.0f);
                damager.playSound(damager.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                victim.sendMessage("§cYou were killed by the murderer.");
            }
        }

        // Handle arrow damage events
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            // Check if the arrow shooter is a player
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                event.setCancelled(true);

                /**
                 * Friendly fire
                 */
                if (victim != gameManager.getMurderer()) {
                    gameManager.handleDeath(victim, false, false);
                    shooter.getInventory().clear();
                    shooter.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 255, true, false));
                    shooter.sendMessage("§cYou have lost all your items due to friendly fire!");
                    victim.sendMessage("§cYou were killed by friendly fire.");
                    return;
                }

                /**
                 * Murderer hit by arrow
                 */
                if (victim == gameManager.getMurderer()) {
                    gameManager.end(false, shooter);
                    gameManager.setPlayerToSpec(victim);
                    victim.getWorld().playEffect(victim.getLocation(), Effect.FIREWORK_SHOOT, 1);
                }
            }
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (!to.getBlock().equals(from.getBlock())) {
            // player in void
            if (to.getY() <= 1) {
                event.setCancelled(true);

                if (gameManager.getStatus() == GameStatus.PLAYING && !gameManager.getSpectators().contains(player)) {
                    gameManager.handleDeath(player, false, true);
                }

                player.teleport(gameManager.getCurrentMap().getRandomSpawn());
                return;
            }

            // if player is spectator
            if (gameManager.getSpectators().contains(player) || player == gameManager.getMurderer()) {
                return;
            }

            // bow system
            Location detectiveBowLocation = gameManager.getDetectiveBowLocation();
            if (detectiveBowLocation == null) return;
            if (player != gameManager.murderer) {
                if (player.getLocation().distanceSquared(detectiveBowLocation) <= 1.0) {

                    gameManager.getDetectiveDeathArmorStand().remove();
                    gameManager.detectiveBowLocation = null;
                    gameManager.bowHolder = player;

                    player.getInventory().clear();
                    player.getInventory().setItem(0, ItemUtil.getDetectiveBow());
                    player.getInventory().setItem(10, new ItemStack(Material.ARROW, 1));
                }
            }
        }

    }


    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        gameManager.handleDeath(event.getEntity(), false, false);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        gameManager.setPlayerToSpec(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameManager.getCurrentMap() != null && gameManager.getCurrentMap().isLoaded()) {
                    player.teleport(gameManager.getCurrentMap().getRandomSpawn()); // Teleport the player to a random spawn point on the current map
                    this.cancel(); // Stop the scheduler once the map is loaded and the player is teleported
                }
            }
        }.runTaskTimer(MurderPlugin.INSTANCE, 0L, 5L);
    }


    @EventHandler
    public void onLeft(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getStatus() != GameStatus.PLAYING) return; // If the game is not in progress, return
        if (player == gameManager.getMurderer()) {
            gameManager.end(false, null); // If the player who left was the murderer, end the game with innocent win and no killer
            return;
        }

        gameManager.removePlayer(player);
    }
}
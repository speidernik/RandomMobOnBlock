package org.Speidernik.randomblock;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final RandomBlock plugin;
    private final Set<UUID> playersToClearInventory;
    private final Set<UUID> allPlayers;
    private Location newSpawnLocation;
    private boolean reset;

    public PlayerDeathListener(RandomBlock plugin) {
        this.plugin = plugin;
        this.playersToClearInventory = new HashSet<>();
        this.allPlayers = new HashSet<>();
        this.reset = false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            message.append("\n");
        }
        for (int i = 0; i < 10; i++) {
            Bukkit.broadcastMessage(message.toString());
        }


        plugin.cleanList();
        newSpawnLocation = getNewSpawnLocation();
        reset = true;
        Objects.requireNonNull(newSpawnLocation.getWorld()).setTime(0);

        allPlayers.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                playersToClearInventory.add(uuid);
                return;
            }
            if (!isPlayerAlive(player)) return;
            resetPlayer(player);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (allPlayers.add(playerUUID) && reset) {
            resetPlayer(event.getPlayer());
        }
        if (playersToClearInventory.remove(playerUUID)) {
            resetPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        event.setRespawnLocation(newSpawnLocation);
        resetPlayer(player, false);
    }

    private void resetPlayer(Player player) {
        resetPlayer(player, true);
    }

    private void resetPlayer(Player player, boolean updateSpawnLocation) {
        if (updateSpawnLocation) {
            player.setBedSpawnLocation(newSpawnLocation, true);
        }
        player.teleport(newSpawnLocation);

        player.setRemainingAir(player.getMaximumAir());
        player.getInventory().clear();
        Objects.requireNonNull(player.getEquipment()).clear();
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setExhaustion(0);
        player.setLevel(0);
        player.setExp(0);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.setFireTicks(0);
        player.setGameMode(GameMode.SURVIVAL);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(1.0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR)).setBaseValue(0.0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)).setBaseValue(0.0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(4.0);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_LUCK)).setBaseValue(0.0);
    }

    public boolean isPlayerAlive(Player player) {
        return player.getHealth() > 0;
    }

    private Location getNewSpawnLocation() {
        World world = Bukkit.getServer().getWorlds().get(0);
        Location currentSpawnLocation = world.getSpawnLocation();
        double x = currentSpawnLocation.getX();
        double z = currentSpawnLocation.getZ() - 10000;
        int y = world.getHighestBlockYAt((int) x, (int) z);
        Location newSpawnLocation = new Location(world, x, y + 1, z);
        world.setSpawnLocation(newSpawnLocation);
        return newSpawnLocation;
    }
}

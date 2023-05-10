package org.Speidernik.randomblock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        }
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setLevel(0);
        player.setExp(0);
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

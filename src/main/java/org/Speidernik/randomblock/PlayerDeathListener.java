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
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class PlayerDeathListener implements Listener {

    private final RandomBlock plugin;
    public Set<UUID> playersToClearInventory;
    public Set<UUID> allPlayers;
    public Boolean reset;
    Location newSpawnLocation;
    

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

        for (UUID uuid : allPlayers) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                playersToClearInventory.add(uuid);
                continue;
            }
            if (!isPlayerAlive(player)) continue;

            resetPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!allPlayers.contains(player.getUniqueId()) && reset) {
            resetPlayer(player);
        }
        if (playersToClearInventory.contains(player.getUniqueId())) {
            resetPlayer(player);
            playersToClearInventory.remove(player.getUniqueId());
        }
        allPlayers.add(player.getUniqueId());
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
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        player.setLevel(0);
        player.setExp(0);
    }

    public boolean isPlayerAlive(Player player) {
        return player.getHealth() > 0;
    }

    private Location getNewSpawnLocation() {
        World world = getServer().getWorlds().get(0);
        Location currentSpawnLocation = world.getSpawnLocation();


        // 10,000 blocks north (negative Z direction) from the current spawn location
        double x = currentSpawnLocation.getX();
        double z = currentSpawnLocation.getZ() - 10000;

        // Get the highest block Y-coordinate at the new spawn location
        int y = world.getHighestBlockYAt((int) x, (int) z);

        // Set the new spawn location one block above the highest block
        Location newSpawnLocation = new Location(world, x, y + 1, z);
        world.setSpawnLocation(newSpawnLocation);
        return newSpawnLocation;

    }
}

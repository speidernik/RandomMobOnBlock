package org.nicemods.randomblock;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDeathListener implements Listener {

    private final RandomBlock plugin;

    public PlayerDeathListener(RandomBlock plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World currentWorld = plugin.getServer().getWorlds().get(0);
        player.teleport(currentWorld.getSpawnLocation());
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Pause time in the current world
        plugin.cleanList();
        World currentWorld = plugin.getServer().getWorlds().get(0);
        currentWorld.setGameRuleValue("doDaylightCycle", "false");

        // Change players' game mode to spectator
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        // Generate new worlds asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String newWorldName = "new_world_" + System.currentTimeMillis();

            Bukkit.getScheduler().runTask(plugin, () -> {
                World newOverworld = generateNewWorld(newWorldName, World.Environment.NORMAL);
                World newNether = generateNewWorld(newWorldName + "_nether", World.Environment.NETHER);
                World newEnd = generateNewWorld(newWorldName + "_the_end", World.Environment.THE_END);

                // Set the server's default spawn point to the new world
                plugin.getServer().getWorlds().get(0).setSpawnLocation(newOverworld.getSpawnLocation());

                // Update the server.properties file to use the new world as the default world
                updateServerProperties("level-name", newWorldName);

                // Teleport players to the new world and change their game mode
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.teleport(newOverworld.getSpawnLocation());
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setBedSpawnLocation(newOverworld.getSpawnLocation(), true);

                    // Set player's health and food level to maximum
                    player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                    player.setFoodLevel(20);
                }

                // Unload the old overworld, Nether, and End
                List<World> oldWorlds = new ArrayList<>(Bukkit.getWorlds());
                for (World oldWorld : oldWorlds) {
                    String oldWorldName = oldWorld.getName();
                    if (oldWorldName.equals(currentWorld.getName()) || oldWorldName.equals(currentWorld.getName() + "_nether") || oldWorldName.equals(currentWorld.getName() + "_the_end")) {
                        Bukkit.unloadWorld(oldWorldName, true);
                    }
                }

                // Delete the old world folders
                for (World oldWorld : oldWorlds) {
                    String oldWorldName = oldWorld.getName();
                    if (oldWorldName.equals(currentWorld.getName()) || oldWorldName.equals(currentWorld.getName() + "_nether") || oldWorldName.equals(currentWorld.getName() + "_the_end")) {
                        File oldWorldFolder = new File(plugin.getDataFolder().getParentFile().getParentFile(), oldWorldName);
                        deleteDirectory(oldWorldFolder);
                    }
                }

                // Resume time in the new world
                newOverworld.setGameRuleValue("doDaylightCycle", "true");
                newNether.setGameRuleValue("doDaylightCycle", "true");
                newEnd.setGameRuleValue("doDaylightCycle", "true");
            });
        });
    }



    private World generateNewWorld(String worldName, World.Environment environment) {
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.environment(environment);
        worldCreator.type(WorldType.NORMAL);
        worldCreator.generateStructures(true);
        worldCreator.seed(System.currentTimeMillis());
        return Bukkit.createWorld(worldCreator);
    }


    public void updateServerProperties(String key, String value) {
        Properties properties = new Properties();
        File propertiesFile = new File("server.properties");

        try {
            // Load the server.properties file
            FileInputStream fileInputStream = new FileInputStream(propertiesFile);
            properties.load(fileInputStream);
            fileInputStream.close();

            // Modify the value of the specified key
            properties.setProperty(key, value);

            // Save the modified server.properties file
            FileOutputStream fileOutputStream = new FileOutputStream(propertiesFile);
            properties.store(fileOutputStream, "Minecraft server properties");
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(new File(directory, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return directory.delete();
    }

}

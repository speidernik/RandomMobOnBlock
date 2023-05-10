package org.Speidernik.randomblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class RandomBlock extends JavaPlugin implements Listener {

    private final Map<String, EntityType> blockMobMap = new HashMap<>();
    private final Random random = new Random();
    private final PlayerDeathListener playerDeathListener;
    private EntityType[] spawnableEntityTypes;

    public RandomBlock() {
        this.playerDeathListener = new PlayerDeathListener(this);
    }


    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(playerDeathListener, this);

        spawnableEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isSpawnable)
                .filter(EntityType::isAlive)
                .toArray(EntityType[]::new);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String blockType = block.getType().toString();

        EntityType entityType = blockMobMap.computeIfAbsent(blockType, this::getRandomSpawnableEntityType);

        if (entityType == null) {
            getLogger().warning("Could not find a spawnable entity for block type " + blockType);
            return;
        }

        if (entityType.getEntityClass() != null) {
            block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), entityType.getEntityClass());
        }
    }

    private EntityType getRandomSpawnableEntityType(String blockType) {
        if (spawnableEntityTypes.length > 0) {
            EntityType randomEntityType = spawnableEntityTypes[random.nextInt(spawnableEntityTypes.length)];
            Bukkit.broadcastMessage(ChatColor.GREEN + "From now on, breaking " + blockType + " blocks will spawn " + randomEntityType.name() + "s.");
            return randomEntityType;
        }
        return null;
    }

    public void cleanList() {
        blockMobMap.clear();
    }
}

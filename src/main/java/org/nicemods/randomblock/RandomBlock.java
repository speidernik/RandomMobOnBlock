package org.nicemods.randomblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class RandomBlock extends JavaPlugin implements Listener {

    private final HashMap<String, EntityType> blockMobMap = new HashMap<>();
    private final Random random = new Random();
    private final PlayerDeathListener playerDeathListener;
    private EntityType[] entityTypes;

    public RandomBlock() {
        this.playerDeathListener = new PlayerDeathListener(this);
    }

    public void cleanList() {
        blockMobMap.clear();
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        playerDeathListener.playersToClearInventory = new HashSet<>();
        playerDeathListener.allPlayers = new HashSet<>();
    }

    private void initializeEntityTypes() {
        entityTypes = EntityType.values();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String blockType = block.getType().toString();

        EntityType entityType;
        if (blockMobMap.containsKey(blockType)) {
            entityType = blockMobMap.get(blockType);
        } else {
            if (entityTypes == null) {
                initializeEntityTypes();
            }
            int randomIndex = random.nextInt(entityTypes.length);
            entityType = entityTypes[randomIndex];
            int count = 0;
            while ((!entityType.isAlive() || !entityType.isSpawnable()) && count < 100) {
                randomIndex = random.nextInt(entityTypes.length);
                entityType = entityTypes[randomIndex];
                count++;
            }
            blockMobMap.put(blockType, entityType);
            Bukkit.broadcastMessage(ChatColor.GREEN + "From now on, breaking " + blockType + " blocks will spawn " + entityType.name() + "s.");
        }

        if (entityType.isAlive() && entityType.isSpawnable()) {
            assert entityType.getEntityClass() != null;
            block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), entityType.getEntityClass());
        } else {
            getLogger().warning("Could not spawn entity of type " + entityType.name() + " because it is not spawnable or alive.");
        }
    }
}

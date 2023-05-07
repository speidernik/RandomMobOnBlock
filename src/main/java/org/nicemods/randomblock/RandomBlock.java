package org.nicemods.randomblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Random;

public class RandomBlock extends JavaPlugin implements Listener {



    private HashMap<String, EntityType> blockMobMap = new HashMap<>();
    private Random random = new Random();

    public void cleanList() {
        blockMobMap.clear();
    }
    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String blockType = block.getType().toString();

        EntityType entityType;
        if (blockMobMap.containsKey(blockType)) {
            entityType = blockMobMap.get(blockType);
        } else {
            EntityType[] entityTypes = EntityType.values();
            int randomIndex = random.nextInt(entityTypes.length);

            entityType = entityTypes[randomIndex];

            while (!entityType.isAlive() || !entityType.isSpawnable()) {
                randomIndex = random.nextInt(entityTypes.length);
                entityType = entityTypes[randomIndex];
            }
            blockMobMap.put(blockType, entityType);
            Bukkit.broadcastMessage(ChatColor.GREEN + "From now on, breaking " + blockType + " blocks will spawn " + entityType.name() + "s.");
        }

        LivingEntity spawnedMob = (LivingEntity) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0.5), entityType);
    }
}

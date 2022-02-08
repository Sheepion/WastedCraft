package com.sheepion.wastedcraft.listener;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;

public class RedstoneListener implements Listener {
    private static final HashMap<String, Double> destroyMap = new HashMap<>();
    private static boolean drop;

    //reload the config
    public static void reload() {
        destroyMap.clear();
        //create config file if not exist
        File redstoneLimitConfigFile = new File(WastedCraft.plugin.getDataFolder(), "/redstoneLimit.yml");
        if (!redstoneLimitConfigFile.exists()) {
            WastedCraft.plugin.saveResource("redstoneLimit.yml", false);
        }
        FileConfiguration redstoneLimitConfig = YamlConfiguration.loadConfiguration(redstoneLimitConfigFile);
        //load redstone limit
        WastedCraft.plugin.getLogger().info("Loading redstone limit...");
        drop = redstoneLimitConfig.getBoolean("drop");
        WastedCraft.plugin.getLogger().info("\t-Drop: " + drop);
        for (String blockType : redstoneLimitConfig.getConfigurationSection("destroy-chance").getKeys(false)) {
            destroyMap.put(blockType.toUpperCase(), redstoneLimitConfig.getDouble("destroy-chance." + blockType));
            WastedCraft.plugin.getLogger().info("\t-Loaded " + blockType + ": " + destroyMap.get(blockType.toUpperCase()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (destroyMap.containsKey(event.getBlock().getType().toString())) {
            if (Math.random() < destroyMap.get(event.getBlock().getType().toString())) {
                Location location = event.getBlock().getLocation();
                //break the block lately to avoid block update
                WastedCraft.plugin.getServer().getScheduler().runTaskLater(WastedCraft.plugin, () -> {
                    //drop item if drop is true
                    if (drop) {
                        for (ItemStack itemStack : event.getBlock().getDrops()) {
                            location.getWorld().dropItemNaturally(location, itemStack);
                        }
                    }
                    event.getBlock().setType(Material.AIR);
                }, 1L);
            }
        }

    }
}

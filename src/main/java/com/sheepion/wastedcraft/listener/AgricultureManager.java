package com.sheepion.wastedcraft.listener;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.HashMap;

public class AgricultureManager implements Listener {
    private static final HashMap<String, Double> composterTable = new HashMap<>();
    private static final HashMap<String, CropExp> expBonusTable = new HashMap<>();

    public static void reload() {
        composterTable.clear();
        expBonusTable.clear();
        //create agriculture config if not exist
        File agricultureConfigFile = new File(WastedCraft.plugin.getDataFolder(), "/agriculture.yml");
        if (!agricultureConfigFile.exists()) {
            WastedCraft.plugin.saveResource("agriculture.yml", false);
        }
        //reload agriculture config
        FileConfiguration agricultureConfig = YamlConfiguration.loadConfiguration(agricultureConfigFile);
        WastedCraft.plugin.getLogger().info("loading agriculture config...");
        //reload composter table
        ConfigurationSection composterSection = agricultureConfig.getConfigurationSection("composter-accept");
        WastedCraft.plugin.getLogger().info("loading composter table...");
        for (String key : composterSection.getKeys(false)) {
            addCompost(key.toUpperCase(), composterSection.getDouble(key));
            WastedCraft.plugin.getLogger().info("\t-Loaded " + key + ": " + composterSection.getDouble(key));
        }
        //load exp bonus
        ConfigurationSection expSection = agricultureConfig.getConfigurationSection("exp-bonus");
        WastedCraft.plugin.getLogger().info("loading exp bonus...");
        for (String key : expSection.getKeys(false)) {
            CropExp bonus = new CropExp(expSection.getDouble(key + ".chance"), expSection.getInt(key + ".amount"));
            expBonusTable.put(key.toUpperCase(), bonus);
        }
    }

    //add composter accept
    public static void addCompost(String type, double compost) {
        composterTable.put(type, compost);
    }

    //remove composter accept
    public static void removeCompost(String type) {
        composterTable.remove(type);
    }

    //return composter success chance
    public static double getCompost(String type) {
        return composterTable.get(type);
    }

    //manage composter
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //player right click composter
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == org.bukkit.Material.COMPOSTER) {
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            BlockData blockData = block.getBlockData();
            Levelled levelled = (Levelled) blockData;
            String handItem = player.getInventory().getItemInMainHand().getType().toString();
            if (levelled.getLevel() < levelled.getMaximumLevel()) {
                if (composterTable.containsKey(handItem)) {
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    player.getWorld().spawnParticle(Particle.COMPOSTER, block.getLocation().add(0.5, 1, 0.5), 8, 0.2, 0, 0.2);
                    if (Math.random() < composterTable.get(handItem)) {
                        levelled.setLevel(levelled.getLevel() + 1);
                        state.setBlockData(blockData);
                        state.update();
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_COMPOSTER_FILL_SUCCESS, 1.3f, 1f);
                    } else {
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_COMPOSTER_FILL, 1.3f, 0.8f);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    //give exp when harvest
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        //check if block is crop
        if (block.getBlockData() instanceof Ageable) {
            //check if config have set exp bonus for this kind of crop.
            if (expBonusTable.containsKey(block.getType().toString())) {
                //check if is mature
                if (((Ageable) block.getBlockData()).getAge() == ((Ageable) block.getBlockData()).getMaximumAge()) {
                    CropExp bonus = expBonusTable.get(block.getType().toString());
                    if (Math.random() < bonus.chance()) {
                        event.setExpToDrop(bonus.amount());
                    }
                }
            }
        }
    }
}

record CropExp(double chance, int amount) {
}

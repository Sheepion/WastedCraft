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
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
            composterAccept(block, player.getInventory().getItemInMainHand());
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

    //add hopper support to composter accept
    @EventHandler(ignoreCancelled = false)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        //check if source is hopper
        if (event.getSource().getType() == InventoryType.HOPPER) {
            //check if destination is composter
            if (event.getDestination().getType() == InventoryType.COMPOSTER) {
                composterAccept(event.getSource().getLocation().subtract(0, 1, 0).getBlock(), event.getItem());
            }
        }
    }

    public static void composterAccept(Block composter, ItemStack item) {
        if (composter.getType() != org.bukkit.Material.COMPOSTER) return;
        BlockState state = composter.getState();
        BlockData blockData = composter.getBlockData();
        Levelled levelled = (Levelled) blockData;
        if (levelled.getLevel() < levelled.getMaximumLevel()) {
            String itemType = item.getType().toString();
            if (composterTable.containsKey(itemType)) {
                item.setAmount(item.getAmount() - 1);
                composter.getWorld().spawnParticle(Particle.COMPOSTER, composter.getLocation().add(0.5, 1, 0.5), 8, 0.2, 0, 0.2);
                if (Math.random() < composterTable.get(itemType)) {
                    levelled.setLevel(levelled.getLevel() + 1);
                    state.setBlockData(blockData);
                    state.update();
                    composter.getWorld().playSound(composter.getLocation(), Sound.BLOCK_COMPOSTER_FILL_SUCCESS, 1.3f, 1f);
                } else {
                    composter.getWorld().playSound(composter.getLocation(), Sound.BLOCK_COMPOSTER_FILL, 1.3f, 0.8f);
                }
            }
        }
    }
}

record CropExp(double chance, int amount) {
}

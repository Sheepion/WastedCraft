package com.sheepion.wastedcraft.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class AgricultureManager implements Listener {
    private static final HashMap<String, Double> composterTable = new HashMap<>();
    public static void preReload(){
        composterTable.clear();
    }

    public static void addCompost(String type, double compost) {
        composterTable.put(type, compost);
    }

    public static void removeCompost(String type) {
        composterTable.remove(type);
    }

    public static double getCompost(String type) {
        return composterTable.get(type);
    }

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

}

package com.sheepion.wastedcraft.listener;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DeathPenalty implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        loseItem(player);
        loseExp(player);
    }

    /**
     * Lose items when a player dies. The chance to lose is depending on player's level.
     *
     * @param player The player who died.
     */
    public static void loseItem(Player player) {
        int level = player.getLevel();
        //lose nothing when level >= 30
        if (level >= 30) {
            return;
        }
        //For every level lower than 30, increase the chance by 1%
        double chance = (30 - level) * 0.01;
        //iterate through all player's inventory, for every item in an itemstack, check if it is lost.
        int totalLost = 0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                int loseAmount = 0;
                for (int j = 0; j < item.getAmount(); j++) {
                    if (Math.random() < chance) {
                        loseAmount++;
                    }
                }
                if (loseAmount == 0) {
                    continue;
                }
                ItemStack drops = item.clone();
                drops.setAmount(loseAmount);
                item.setAmount(item.getAmount() - loseAmount);
                totalLost += loseAmount;
                player.getWorld().dropItemNaturally(player.getLocation(), drops);
            }
        }
        player.sendMessage("§c你失去了 " + totalLost + " 个物品!");
    }

    /**
     * Lose experience when a player dies.
     *
     * @param player The player who died.
     */
    public static void loseExp(Player player) {
        World world = player.getWorld();
        //only manage death penalty in world that keepInventory is true
        if (Boolean.FALSE.equals(world.getGameRuleValue(GameRule.KEEP_INVENTORY))) {
            return;
        }
        //death penalty in main world
        if (world.getEnvironment() == World.Environment.NORMAL) {
            //lose 0%~10% total experience
            int levelLost = (int) Math.ceil(player.getLevel() * Math.random() * 0.1);
            player.sendMessage("§c你失去了 " + levelLost + " 级!");
            player.setLevel(player.getLevel() - levelLost);
        }
        //death penalty in other worlds
        else {
            //lose 10%~50% total experience
            int levelLost = (int) Math.ceil(player.getLevel() * (Math.random() * 0.4 + 0.1));
            player.sendMessage("§c你失去了 " + levelLost + " 级!");
            player.setLevel(player.getLevel() - levelLost);
        }
    }
}

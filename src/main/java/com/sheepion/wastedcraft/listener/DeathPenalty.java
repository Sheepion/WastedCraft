package com.sheepion.wastedcraft.listener;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathPenalty implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        //only manage death penalty in world that keepInventory is true
        if (!world.getGameRuleValue(GameRule.KEEP_INVENTORY)) {
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

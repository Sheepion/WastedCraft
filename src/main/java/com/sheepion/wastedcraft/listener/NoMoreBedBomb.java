package com.sheepion.wastedcraft.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class NoMoreBedBomb implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getBlock().getType().toString().contains("BED")) {
            if (event.getBlock().getWorld().getEnvironment()!= World.Environment.NORMAL) {
                event.setCancelled(true);
            }
        }
    }
}

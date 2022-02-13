package com.sheepion.wastedcraft.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobFarmListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        //no more iron farm
        if(event.getEntity().getType()== EntityType.IRON_GOLEM) {
            event.getDrops().removeIf(itemStack -> itemStack.getType() == org.bukkit.Material.IRON_INGOT);
        }
    }
}

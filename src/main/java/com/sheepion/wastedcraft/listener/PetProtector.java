package com.sheepion.wastedcraft.listener;

import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class PetProtector implements org.bukkit.event.Listener {
    //cancel pet's damage
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        //protect cat
        if (event.getEntity().getType().equals(org.bukkit.entity.EntityType.CAT)) {
            event.setCancelled(true);
        }
        //protect tamed wolf
        if (event.getEntity().getType().equals(org.bukkit.entity.EntityType.WOLF) && ((Tameable) event.getEntity()).isTamed()) {
            event.setCancelled(true);
        }
        //protect tamed animal
        if (event.getEntity() instanceof Tameable) {
            if (((Tameable) event.getEntity()).isTamed()) {
                event.setCancelled(true);
            }
        }
    }
}

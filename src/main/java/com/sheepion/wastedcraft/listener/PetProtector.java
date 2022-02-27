package com.sheepion.wastedcraft.listener;

import com.google.common.base.Supplier;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.stream.Stream;

public class PetProtector implements org.bukkit.event.Listener {
    //cancel pet's damage
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        //protect cat
        if (event.getEntity().getType().equals(org.bukkit.entity.EntityType.CAT)) {
            event.setCancelled(true);
        }
        //protect tamed animal
        if (event.getEntity() instanceof Tameable) {
            if (((Tameable) event.getEntity()).isTamed()) {
                event.setCancelled(true);
            }
        }
    }

    //set player's tamed entities' coordinate to player
    public static void findTamedEntity(Player player) {
        Supplier<Stream<Entity>> tamedEntity = () -> player.getWorld().getEntities().stream().filter(entity ->
                (entity instanceof Tameable) && ((Tameable) entity).isTamed() && player.equals(((Tameable) entity).getOwner())
        );
        if (tamedEntity.get().findAny().isPresent()) {
            player.sendMessage(ChatColor.GREEN + "--------------在当前世界找到 " + tamedEntity.get().count() + " 个宠物--------------");
            tamedEntity.get().forEach(entity -> {
                Location l = entity.getLocation();
                double distance = player.getLocation().distance(l);
                player.sendMessage(ChatColor.GOLD + " 类型: " + ChatColor.BLUE + entity.getType().toString()
                        + ChatColor.GOLD + " 名称: " + ChatColor.BLUE + entity.getCustomName()
                        + ChatColor.GOLD + " 坐标: " + ChatColor.BLUE + "X: " + (int) l.getX() + " Y: " + (int) l.getY() + " Z: " + (int) l.getZ()
                        + ChatColor.GOLD + "  [" + ChatColor.BLUE + (int) distance + "米" + ChatColor.GOLD + "]");
            });
        } else {
            player.sendMessage(ChatColor.RED + "当前世界没有找到宠物");
        }

    }
}

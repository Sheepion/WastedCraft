package com.sheepion.wastedcraft.item;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class TeleportPotion implements Listener {
    private static final ItemStack newPotion;
    private static final NamespacedKey TP_WORLD = new NamespacedKey(WastedCraft.plugin, "tp_world");
    private static final NamespacedKey TP_X = new NamespacedKey(WastedCraft.plugin, "tp_x");
    private static final NamespacedKey TP_Y = new NamespacedKey(WastedCraft.plugin, "tp_y");
    private static final NamespacedKey TP_Z = new NamespacedKey(WastedCraft.plugin, "tp_z");
    private static final NamespacedKey TP_YAW = new NamespacedKey(WastedCraft.plugin, "tp_yaw");
    private static final NamespacedKey TP_PITCH = new NamespacedKey(WastedCraft.plugin, "tp_pitch");

    public static ItemStack getPotion() {
        return newPotion;
    }

    static {
        newPotion = new ItemStack(Material.POTION, 1);
        ItemMeta itemMeta = newPotion.getItemMeta();
        assert itemMeta != null;
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§f潜行时右键来设置传送目的地");
        itemMeta.setLore(lore);
        itemMeta.setDisplayName("§b传送药水");
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(TP_WORLD, PersistentDataType.STRING, "NULL__");
        newPotion.setItemMeta(itemMeta);

    }

    //add location to pdc when right click
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //only right click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        //only sneaking
        if (!player.isSneaking()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return;
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        //only teleport potion that have no location set yet.
        if ("NULL__".equals(pdc.get(TP_WORLD, PersistentDataType.STRING))) {
            Location location = player.getLocation();
            pdc.set(TP_WORLD, PersistentDataType.STRING, location.getWorld().getName());
            pdc.set(TP_X, PersistentDataType.DOUBLE, location.getX());
            pdc.set(TP_Y, PersistentDataType.DOUBLE, location.getY());
            pdc.set(TP_Z, PersistentDataType.DOUBLE, location.getZ());
            pdc.set(TP_YAW, PersistentDataType.FLOAT, location.getYaw());
            pdc.set(TP_PITCH, PersistentDataType.FLOAT, location.getPitch());
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + "目标世界: " + location.getWorld().getName());
            lore.add(ChatColor.WHITE + "目标坐标: " + (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ());
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            player.getInventory().setItemInMainHand(item);
            player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    //teleport player to location after drinking teleport potion
    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return;
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        String world = pdc.get(TP_WORLD, PersistentDataType.STRING);
        if (world == null || world.equals("NULL__")) return;
        double x = pdc.get(TP_X, PersistentDataType.DOUBLE);
        double y = pdc.get(TP_Y, PersistentDataType.DOUBLE);
        double z = pdc.get(TP_Z, PersistentDataType.DOUBLE);
        float yaw = pdc.get(TP_YAW, PersistentDataType.FLOAT);
        float pitch = pdc.get(TP_PITCH, PersistentDataType.FLOAT);
        Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        Player player = event.getPlayer();
        player.spawnParticle(Particle.PORTAL, player.getLocation(), (int) ((Math.random() * 200)), 0.5, 0.5, 0.5);
        //teleport vehicle first.
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
            vehicle.teleport(location);
            //simply add player as vehicle's passenger will cause bug, player don't actually get into vehicle.
        }
        //teleport player.
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.spawnParticle(Particle.PORTAL, location, (int) ((Math.random() * 200)), 0.5, 0.5, 0.5);
    }
}

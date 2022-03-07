package com.sheepion.wastedcraft.item;

import com.sheepion.custompotionapi.CustomPotionEffect;
import com.sheepion.custompotionapi.CustomPotionEffectProperty;
import com.sheepion.custompotionapi.CustomPotionEffectType;
import com.sheepion.custompotionapi.CustomPotionManager;
import com.sheepion.wastedcraft.WastedCraft;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.item
 *
 * @author Sheepion
 * @date 3/6/2022
 */
public class TeleportPotionEffectType implements CustomPotionEffectType, Listener {
    private static final NamespacedKey TP_WORLD = new NamespacedKey(WastedCraft.plugin, "tp_world");
    private static final NamespacedKey TP_X = new NamespacedKey(WastedCraft.plugin, "tp_x");
    private static final NamespacedKey TP_Y = new NamespacedKey(WastedCraft.plugin, "tp_y");
    private static final NamespacedKey TP_Z = new NamespacedKey(WastedCraft.plugin, "tp_z");
    private static final NamespacedKey TP_YAW = new NamespacedKey(WastedCraft.plugin, "tp_yaw");
    private static final NamespacedKey TP_PITCH = new NamespacedKey(WastedCraft.plugin, "tp_pitch");
    /**
     * the progress of being teleported
     * player should be teleported when progress is 360
     */
    private static final HashMap<UUID, Double> waitingProgress = new HashMap<>();

    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey(WastedCraft.plugin, "potion.teleport");
    }

    @Override
    public boolean canBeApplied(LivingEntity entity) {
        return true;
    }

    @Override
    public boolean canBeRemovedByMilk(LivingEntity entity, CustomPotionEffectProperty property) {
        return false;
    }

    @Override
    public boolean spawnAreaEffectCloudOnCreeperExplosion(Creeper creeper, CustomPotionEffectProperty property) {
        return true;
    }

    @Override
    public void beforeApply(LivingEntity entity, CustomPotionEffectProperty property) {
        if (CustomPotionManager.isPotionEffectActive(entity.getUniqueId(), this)) {
            property.setRestDuration(-1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().setItem(0, CustomPotionManager.getLingeringPotion(getKey(), 20, 1, 4, 0));
        player.getInventory().setItem(1, CustomPotionManager.getSplashPotion(getKey(), 20, 1, 4, 0));
        player.getInventory().setItem(2, CustomPotionManager.getPotion(getKey(), 20, 1, 4, 0));
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
        //only teleport potion
        CustomPotionEffect potionEffect = CustomPotionManager.getCustomPotionEffect(item);
        if (potionEffect == null || !potionEffect.getEffectType().getKey().equals(getKey())) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return;
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        //only teleport potion that have no location set yet.
        if (pdc.has(TP_WORLD, PersistentDataType.STRING)) return;
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
        event.setCancelled(true);
    }

    @Override
    public void effect(LivingEntity entity, CustomPotionEffectProperty property) {
        //draw a circle portal particle around the entity
        Location entityLocation = entity.getLocation();
        double height = entity.getBoundingBox().getHeight();
        double radius = Math.max(entity.getBoundingBox().getWidthX(), entity.getBoundingBox().getWidthZ()) / 2 + 0.3;
        if (!waitingProgress.containsKey(entity.getUniqueId())) {
            waitingProgress.put(entity.getUniqueId(), 0d);
        }
        waitingProgress.put(entity.getUniqueId(), waitingProgress.get(entity.getUniqueId()) + 360 / (1.0 * property.getDuration() / property.getCheckInterval()));
        double angdeg = waitingProgress.get(entity.getUniqueId());
        for (double offset = -18 * property.getCheckInterval(); offset <= 18 * property.getCheckInterval(); offset += 1) {
            double px = entityLocation.getX() + Math.cos(Math.toRadians((angdeg + offset) * 5)) * radius;
            double py = entityLocation.getY() - 1 + height / (2 * Math.PI) * Math.toRadians(angdeg + offset);
            double pz = entityLocation.getZ() + Math.sin(Math.toRadians((angdeg + offset) * 5)) * radius;
            entityLocation.getWorld().spawnParticle(Particle.PORTAL, px, py, pz, 0, 0, 0, 0, 0);
        }
        if (angdeg >= 360) {
            waitingProgress.put(entity.getUniqueId(), 0d);
        }
        if (property.getRestDuration() >= property.getCheckInterval()) {
            return;
        }
        ItemStack potion = property.getPotion();
        ItemMeta itemMeta = potion.getItemMeta();
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
        location.getChunk().load();
        entity.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation(), (int) ((Math.random() * 200)), 0.5, 0.5, 0.5);
        //teleport vehicle first.
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
            vehicle.teleport(location);
            //simply add player as vehicle's passenger will cause bug, player don't actually get into vehicle.
        }
        Chunk originChunk = entity.getLocation().getChunk();
        //teleport player.
        entity.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        entity.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        entity.getWorld().spawnParticle(Particle.PORTAL, location, (int) ((Math.random() * 200)), 0.5, 0.5, 0.5);
        //keep the origin chunk loaded to teleport pets.
        WastedCraft.plugin.getServer().getScheduler().runTaskLater(WastedCraft.plugin, () -> originChunk.load(), 20);
    }

    @Override
    public @Nullable ArrayList<PotionMix> potionMixes() {
        ArrayList<PotionMix> potionMixes = new ArrayList<>();
        //add brewer recipe
        ItemStack result = CustomPotionManager.getPotion(getKey(), 1, 1, 1, 5);
        RecipeChoice input = new RecipeChoice.ExactChoice(CustomPotionManager.getPotion(Material.POTION, PotionType.WATER));
        RecipeChoice ingredient = new RecipeChoice.MaterialChoice(Material.ENDER_PEARL);
        PotionMix waterMix = new PotionMix(new NamespacedKey(WastedCraft.plugin, "potionmix.teleport"), result, input, ingredient);
        potionMixes.add(waterMix);
        return potionMixes;
    }

    @Override
    public Component potionDisplayName(CustomPotionEffectProperty property) {
        return Component.text("§b传送药水");
    }

    @Override
    public ArrayList<Component> potionLore(CustomPotionEffectProperty property) {
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("§f潜行时右键来设置传送目的地"));
        return lore;
    }

    @Override
    public Color potionColor(CustomPotionEffectProperty property) {
        return Color.FUCHSIA;
    }

    @Override
    public ArrayList<Component> splashPotionLore(CustomPotionEffectProperty property) {
        return potionLore(property);
    }

    @Override
    public Component splashPotionDisplayName(CustomPotionEffectProperty property) {
        return potionDisplayName(property);
    }

    @Override
    public Color splashPotionColor(CustomPotionEffectProperty property) {
        return potionColor(property);
    }

    @Override
    public ArrayList<Component> lingeringPotionLore(CustomPotionEffectProperty property) {
        return potionLore(property);
    }

    @Override
    public Component lingeringPotionDisplayName(CustomPotionEffectProperty property) {
        return potionDisplayName(property);
    }

    @Override
    public Color lingeringPotionColor(CustomPotionEffectProperty property) {
        return potionColor(property);
    }

    @Override
    public int areaEffectCloudReapplicationDelay(CustomPotionEffectProperty property) {
        return property.getDuration() * 2;
    }
}

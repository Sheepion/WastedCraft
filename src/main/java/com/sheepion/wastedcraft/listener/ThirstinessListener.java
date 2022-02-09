package com.sheepion.wastedcraft.listener;

import com.sheepion.wastedcraft.WastedCraft;
import com.sheepion.wastedcraft.api.SendStateTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static com.sheepion.wastedcraft.api.Thirstiness.*;
import static com.sheepion.wastedcraft.listener.ThirstinessListener.*;

public class ThirstinessListener implements Listener {
    private static double baseDecrease;
    private static long taskInterval;
    private static double dropToZeroDamage;
    private static double lossPerBlockBreak;
    private static double lossPerBlockPlace;
    private static double runCost;
    private static double regainHealthThreshold;
    private static double regainHealthCost;
    private static double netherCost;
    private static final HashSet<UUID> lastDamageCausedByThirsty = new HashSet<>();
    private static final HashMap<UUID, BukkitTask> updateThirstinessTasks = new HashMap<>();
    private static final HashMap<String, Double> thirstinessTable = new HashMap<>();

    public static void preReload(){
        thirstinessTable.clear();
    }
    public static double getNetherCost() {
        return netherCost;
    }

    public static void setNetherCost(double netherCost) {
        ThirstinessListener.netherCost = netherCost;
    }

    public static double getRegainHealthThreshold() {
        return regainHealthThreshold;
    }

    public static void setRegainHealthThreshold(double regainHealthThreshold) {
        ThirstinessListener.regainHealthThreshold = regainHealthThreshold;
    }

    public static double getRegainHealthCost() {
        return regainHealthCost;
    }

    public static void setRegainHealthCost(double regainHealthCost) {
        ThirstinessListener.regainHealthCost = regainHealthCost;
    }

    public static double getLossPerBlockPlace() {
        return lossPerBlockPlace;
    }

    public static void setLossPerBlockPlace(double lossPerBlockPlace) {
        ThirstinessListener.lossPerBlockPlace = lossPerBlockPlace;
    }

    public static double getBaseDecrease() {
        return baseDecrease;
    }

    public static void setBaseDecrease(double baseDecrease) {
        ThirstinessListener.baseDecrease = baseDecrease;
    }

    public static long getTaskInterval() {
        return taskInterval;
    }

    public static void setTaskInterval(long taskInterval) {
        ThirstinessListener.taskInterval = taskInterval;
    }

    public static double getDropToZeroDamage() {
        return dropToZeroDamage;
    }

    public static void setDropToZeroDamage(double dropToZeroDamage) {
        ThirstinessListener.dropToZeroDamage = dropToZeroDamage;
    }

    public static double getLossPerBlockBreak() {
        return lossPerBlockBreak;
    }

    public static void setLossPerBlockBreak(double lossPerBlockBreak) {
        ThirstinessListener.lossPerBlockBreak = lossPerBlockBreak;
    }

    public static double getRunCost() {
        return runCost;
    }

    public static void setRunCost(double runCost) {
        ThirstinessListener.runCost = runCost;
    }

    public static HashMap<String, Double> getThirstinessTable() {
        return thirstinessTable;
    }

    //add task to online players. This should only be called once
    public ThirstinessListener() {
        for (Player player : WastedCraft.plugin.getServer().getOnlinePlayers()) {
            BukkitTask task = WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin
                    , new ThirstyTimelyDecreaseTask(player), taskInterval, taskInterval);
            updateThirstinessTasks.put(player.getUniqueId(), task);
        }
    }

    /**
     * decrease thirstiness when player place a block
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        decreaseAndDamage(player, lossPerBlockPlace, dropToZeroDamage);
    }

    /**
     * decrease thirstiness when player break a block
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        decreaseAndDamage(player, lossPerBlockBreak, dropToZeroDamage);
    }

    //decrease thirstiness when player regain health because of food satisfied.
    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        //cancel event if thirstiness is not enough.
        if (getThirstiness(player) < regainHealthThreshold) {
            player.setFoodLevel(player.getFoodLevel() + 1);
            event.setCancelled(true);
            return;
        }
        decreaseAndDamage(player, regainHealthCost * event.getAmount(), dropToZeroDamage);
    }

    /*decrease players' thirstiness by 0.5 every 30 seconds(600 ticks)
          and decrease player's thirstiness everytime player joins the server*/
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        //decrease thirstiness by 0.02 every 30 ticks after player joins the server 30 ticks later
        BukkitTask task = WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin
                , new ThirstyTimelyDecreaseTask(player), taskInterval, taskInterval);
        updateThirstinessTasks.put(player.getUniqueId(), task);
    }

    /**
     * cancel the task when player leaves the server
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BukkitTask task = updateThirstinessTasks.get(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        updateThirstinessTasks.remove(player.getUniqueId());
    }

    //set thirstiness to 20 when player respawn
    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        setThirstiness(player, 20);
    }

    //called when player drinks water(potion or something)
    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!thirstinessTable.containsKey(item.getType().toString())) {
            return;
        }
        Player player = event.getPlayer();
        increaseThirstiness(player, thirstinessTable.get(item.getType().toString()));
        SendStateTask.sendState(player);
    }


    //send death message if player is killed by thirstiness
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (lastDamageCausedByThirsty.contains(player.getUniqueId())) {
            event.setDeathMessage(ChatColor.WHITE + player.getName() + "渴死了");
            lastDamageCausedByThirsty.remove(player.getUniqueId());
        }
    }

    //remove player from lastDamageCausedByThirsty when player was damaged by something else
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!event.getCause().toString().equals("CUSTOM") && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            lastDamageCausedByThirsty.remove(player.getUniqueId());
        }
    }

    //decrease thirstiness and damage player if thirstiness is below 0
    public static void decreaseAndDamage(Player player, double amount, double damage) {
        //damage player if thirstiness is 0
        if (decreaseThirstiness(player, amount) == 0) {
            player.damage(damage);
            lastDamageCausedByThirsty.add(player.getUniqueId());

        }
    }

    //send thirstiness to player
    public static void sendThirstiness(Player player) {
        double thirstiness = getThirstiness(player);
        thirstiness = Math.round(thirstiness * 100) / 100.0;
        if (thirstiness > 20) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR
                    , new TextComponent(ChatColor.AQUA + "口渴度: " + ChatColor.GREEN + "20+"));
        } else if (thirstiness > 10) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR
                    , new TextComponent(ChatColor.AQUA + "口渴度: " + ChatColor.GREEN + thirstiness));
        } else if (thirstiness > 5) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR
                    , new TextComponent(ChatColor.AQUA + "口渴度: " + ChatColor.BLUE + thirstiness));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR
                    , new TextComponent(ChatColor.AQUA + "口渴度: " + ChatColor.DARK_RED + thirstiness));
        }
    }
}

class ThirstyTimelyDecreaseTask implements Runnable {
    private final Player player;

    public ThirstyTimelyDecreaseTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        double amount = getBaseDecrease();
        //add run cost
        if (player.isSprinting()) {
            amount += getRunCost();
        }
        //add nether cost
        if (player.getLocation().getWorld().getEnvironment() == World.Environment.NETHER) {
            amount += getNetherCost();
        }
        decreaseAndDamage(player, amount, getDropToZeroDamage());
    }
}

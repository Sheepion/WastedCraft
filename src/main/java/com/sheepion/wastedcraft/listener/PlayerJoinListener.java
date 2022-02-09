package com.sheepion.wastedcraft.listener;

import com.sheepion.wastedcraft.WastedCraft;
import com.sheepion.wastedcraft.api.SendStateTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private static final HashMap<UUID, BukkitTask> taskMap=new HashMap<>( );
    public PlayerJoinListener(){
        for (Player onlinePlayer : WastedCraft.plugin.getServer().getOnlinePlayers()) {
            taskMap.put(onlinePlayer.getUniqueId(),WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin, new SendStateTask(onlinePlayer), 10L, 30L));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        taskMap.put(event.getPlayer().getUniqueId(),WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin, new SendStateTask(event.getPlayer()), 10L, 30L));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BukkitTask task = taskMap.get(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        taskMap.remove(player.getUniqueId());
    }
}

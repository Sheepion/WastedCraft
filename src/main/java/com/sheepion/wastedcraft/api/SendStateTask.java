package com.sheepion.wastedcraft.api;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.sheepion.wastedcraft.api.Thirstiness.getThirstiness;

public class SendStateTask implements Runnable {

    private final Player player;

    public SendStateTask(Player player) {
        this.player = player;
    }

    //get thirstiness of player
    public static String getThirstinessString(Player player) {
        double thirstiness = getThirstiness(player);
        thirstiness = Math.round(thirstiness * 100) / 100.0;
        if (thirstiness > 20) {
            return ChatColor.AQUA + "口渴度: " + ChatColor.GREEN + "20+";
        } else if (thirstiness > 10) {
            return ChatColor.AQUA + "口渴度: " + ChatColor.GREEN + thirstiness;
        } else if (thirstiness > 5) {
            return ChatColor.AQUA + "口渴度: " + ChatColor.BLUE + thirstiness;
        } else {
            return ChatColor.AQUA + "口渴度: " + ChatColor.DARK_RED + thirstiness;
        }
    }

    //get temperature of player
    public static String getTemperatureString(Player player) {
        double temperature=player.getLocation().getBlock().getTemperature() * 100;
        temperature = Math.round(temperature*100) / 100.0;
        return ChatColor.YELLOW + ("  温度: " + temperature);
    }

    public static void sendState(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR
                , new TextComponent(getThirstinessString(player)
                        + getTemperatureString(player)));
    }

    @Override
    public void run() {
        sendState(player);
    }
}

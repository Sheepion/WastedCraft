package com.sheepion.wastedcraft.listener;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.io.File;
import java.util.HashMap;

public class SpeedLimit implements Listener {
    private static final HashMap<String,Double> vehicleMaxSpeed= new HashMap<>();
    private static double elytraMaxSpeed;

    //reload config
    public static void reload() {
        vehicleMaxSpeed.clear();
        //create config file if not exist
        File speedLimitConfigFile = new File(WastedCraft.plugin.getDataFolder(), "/speedLimit.yml");
        if (!speedLimitConfigFile.exists()) {
            WastedCraft.plugin.saveResource("speedLimit.yml", false);
        }
        FileConfiguration speedLimitConfig= YamlConfiguration.loadConfiguration(speedLimitConfigFile);
        //load vehicle speed limit
        WastedCraft.plugin.getLogger().info("Loading vehicle speed limit...");
        for(String vehicleType:speedLimitConfig.getConfigurationSection("vehicle-max-speed").getKeys(false)){
            vehicleMaxSpeed.put(vehicleType.toUpperCase(),speedLimitConfig.getDouble("vehicle-max-speed."+vehicleType));
            WastedCraft.plugin.getLogger().info("\t-Loaded "+vehicleType+": "+vehicleMaxSpeed.get(vehicleType.toUpperCase()));
        }
        //load elytra speed limit
        WastedCraft.plugin.getLogger().info("Loading elytra speed limit...");
        elytraMaxSpeed=speedLimitConfig.getDouble("elytra-max-speed");
        WastedCraft.plugin.getLogger().info("\t-elytra-max-speed: "+elytraMaxSpeed);
    }

    //set vehicle max speed. Boat(bugged), minecart.
    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if(event.getEntered() instanceof Player){
            Player player=(Player)event.getEntered();
            Vehicle vehicle=event.getVehicle();
            if(vehicle instanceof Minecart){
                ((Minecart)vehicle).setMaxSpeed(vehicleMaxSpeed.get("MINECART"));
                player.sendMessage("max speed: "+vehicleMaxSpeed.get("MINECART"));
            }else if(vehicle instanceof Boat){
                ((Boat)vehicle).setMaxSpeed(vehicleMaxSpeed.get("BOAT"));
                player.sendMessage("max speed: "+vehicleMaxSpeed.get("BOAT"));
            }
        }
    }

    //player move speed limit
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player=event.getPlayer();
        //elytra speed limit.
        if(player.isGliding()){
            if(player.getVelocity().length()>elytraMaxSpeed){
                player.setVelocity(player.getVelocity().normalize().multiply(elytraMaxSpeed));
            }
            return;
        }
    }
}

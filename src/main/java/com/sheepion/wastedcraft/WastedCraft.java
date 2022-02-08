package com.sheepion.wastedcraft;

import com.sheepion.wastedcraft.command.WastedCraftCommand;
import com.sheepion.wastedcraft.listener.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public final class WastedCraft extends JavaPlugin {
    public static JavaPlugin plugin;

    public WastedCraft() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        //load the config
        reload();
        //register events
        getServer().getPluginManager().registerEvents(new ThirstinessListener(), this);
        getServer().getPluginManager().registerEvents(new PetProtector(),this);
        getServer().getPluginManager().registerEvents(new DeathPenalty(),this);
        getServer().getPluginManager().registerEvents(new AgricultureManager(),this);
        getServer().getPluginManager().registerEvents(new SpeedLimit(),this);
        getServer().getPluginManager().registerEvents(new RedstoneListener(),this);
        //register commands
        getCommand("wastedcraft").setExecutor(new WastedCraftCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void reload() {
        SpeedLimit.reload();
        RedstoneListener.reload();
        //pre-reload
        ThirstinessListener.preReload();
        AgricultureManager.preReload();
        //thirsty config
        File thirstyConfigFile = new File(plugin.getDataFolder(), "/thirsty.yml");
        if (!thirstyConfigFile.exists()) {
            plugin.saveResource("thirsty.yml", false);
        }
        //reload thirsty config
        FileConfiguration thirstyConfig = YamlConfiguration.loadConfiguration(thirstyConfigFile);
        //reload decrease option
        ConfigurationSection decreaseSection = thirstyConfig.getConfigurationSection("decrease");
        assert decreaseSection != null;
        ThirstinessListener.setBaseDecrease(decreaseSection.getDouble("base"));
        ThirstinessListener.setTaskInterval(decreaseSection.getInt("interval"));
        ThirstinessListener.setDropToZeroDamage(decreaseSection.getDouble("drop-to-zero-damage"));
        ThirstinessListener.setLossPerBlockBreak(decreaseSection.getDouble("loss-per-block-break"));
        ThirstinessListener.setLossPerBlockPlace(decreaseSection.getDouble("loss-per-block-place"));
        ThirstinessListener.setRunCost(decreaseSection.getDouble("run-cost"));
        ThirstinessListener.setRegainHealthThreshold(decreaseSection.getDouble("regain-health-threshold"));
        ThirstinessListener.setRegainHealthCost(decreaseSection.getDouble("regain-health-cost"));
        ThirstinessListener.setNetherCost(decreaseSection.getDouble("nether-cost"));
        //reload thirsty source
        HashMap<String, Double> thirstyTable = ThirstinessListener.getThirstinessTable();
        plugin.getLogger().info("loading thirsty config...");
        plugin.getLogger().info("loading thirsty source...");
        for (String key : thirstyConfig.getConfigurationSection("source").getKeys(false)) {
            thirstyTable.put(key.toUpperCase(), thirstyConfig.getDouble("source." + key));
            plugin.getLogger().info("\t-Loaded " + key + ": " + thirstyConfig.getDouble("source." + key));
        }

        //agriculture config
        File agricultureConfigFile = new File(plugin.getDataFolder(), "/agriculture.yml");
        if (!agricultureConfigFile.exists()) {
            plugin.saveResource("agriculture.yml", false);
        }
        //reload agriculture config
        FileConfiguration agricultureConfig= YamlConfiguration.loadConfiguration(agricultureConfigFile);
        plugin.getLogger().info("loading agriculture config...");
        //reload composter table
        ConfigurationSection composterSection = agricultureConfig.getConfigurationSection("composter-accept");
        plugin.getLogger().info("loading composter table...");
        for(String key : composterSection.getKeys(false)){
            AgricultureManager.addCompost(key.toUpperCase(),composterSection.getDouble(key));
            plugin.getLogger().info("\t-Loaded " + key + ": " + composterSection.getDouble(key));
        }
    }

}
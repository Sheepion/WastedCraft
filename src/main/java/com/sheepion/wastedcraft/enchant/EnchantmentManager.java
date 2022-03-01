package com.sheepion.wastedcraft.enchant;

import com.sheepion.wastedcraft.WastedCraft;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.enchant
 *
 * @author Sheepion
 * @date 2/27/2022
 */
public class EnchantmentManager implements Listener {
    private static RangeMining rangeMining = new RangeMining(new NamespacedKey(WastedCraft.plugin, "enchant.range_mining"));

    /**
     * Register all enchantments
     */
    public static void registerEnchantments() {
        registerEnchantment(rangeMining);
        //register listener
        WastedCraft.plugin.getServer().getPluginManager().registerEvents(rangeMining, WastedCraft.plugin);
    }


    /**
     * add custom enchantments when enchant on enchant table
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (event.getExpLevelCost() != 30) {
            return;
        }
        if (rangeMining.canEnchantItem(event.getItem())) {
            for (Enchantment enchantment : event.getEnchantsToAdd().keySet()) {
                if (rangeMining.conflictsWith(enchantment)) {
                    return;
                }
            }
            //event.getEnchantsToAdd().put(rangeMining, 1);
            event.getItem().addUnsafeEnchantment(rangeMining, 1);
            var lore = event.getItem().lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(Component.text(rangeMining.getDisplayName()));
            event.getItem().lore(lore);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (event.getResult() == null) {
            return;
        }
        //add custom enchantments to result
        if (event.getInventory().getItem(0).containsEnchantment(rangeMining)) {
            event.getResult().addUnsafeEnchantment(rangeMining, 1);
        }
        //check conflicts
        if (event.getResult().containsEnchantment(Enchantment.getByKey(rangeMining.getKey()))) {
            for (Enchantment enchantment : event.getResult().getEnchantments().keySet()) {
                if (rangeMining.conflictsWith(enchantment)) {
                    event.setResult(null);
                }
            }
        }
    }

    /**
     * reload enchantments config
     */
    public static void reload() {
        File enchantmentConfigFile = new File(WastedCraft.plugin.getDataFolder(), "enchantment.yml");
        if (!enchantmentConfigFile.exists()) {
            WastedCraft.plugin.saveResource("enchantment.yml", false);
        }
        FileConfiguration enchantmentConfig = YamlConfiguration.loadConfiguration(enchantmentConfigFile);
        rangeMining.reload(enchantmentConfig.getConfigurationSection("enchantments.range_mining"));
    }


    /**
     * Register an enchantment
     *
     * @param enchantment The enchantment to register
     */
    private static void registerEnchantment(Enchantment enchantment) {
        boolean registered = true;
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            registered = false;
            e.printStackTrace();
        }
        if (registered) {
            WastedCraft.plugin.getLogger().info("Registered enchantment: " + enchantment.getKey());
        }
    }
}

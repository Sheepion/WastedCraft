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
    private static ArrayList<CustomEnchantment> registeredEnchantments = new ArrayList<>();
    private static RangeMining rangeMining = new RangeMining(new NamespacedKey(WastedCraft.plugin, "enchant.range_mining"));
    private static LightningArrow lightningArrow = new LightningArrow(new NamespacedKey(WastedCraft.plugin, "enchant.lightning_arrow"));
    private static ExplosiveArrow explosiveArrow = new ExplosiveArrow(new NamespacedKey(WastedCraft.plugin, "enchant.explosive_arrow"));
    private static TrackingArrow trackingArrow= new TrackingArrow(new NamespacedKey(WastedCraft.plugin, "enchant.tracking_arrow"));
    /**
     * Register all enchantments
     */
    public static void registerEnchantments() {
        registerEnchantment(rangeMining);
        registeredEnchantments.add(rangeMining);
        registerEnchantment(lightningArrow);
        registeredEnchantments.add(lightningArrow);
        registerEnchantment(explosiveArrow);
        registeredEnchantments.add(explosiveArrow);
        registerEnchantment(trackingArrow);
        registeredEnchantments.add(trackingArrow);
        registeredEnchantments.sort((CustomEnchantment o1, CustomEnchantment o2) -> o2.getPriority() - o1.getPriority());
        //register listener
        for (CustomEnchantment registeredEnchantment : registeredEnchantments) {
            WastedCraft.plugin.getServer().getPluginManager().registerEvents((Listener) registeredEnchantment, WastedCraft.plugin);
        }
    }


    /**
     * add custom enchantments when enchant on enchant table
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        //only accept 30 levels enchantment
        if (event.getExpLevelCost() != 30) {
            return;
        }
        for (Enchantment registeredEnchantment : registeredEnchantments) {
            if (registeredEnchantment.canEnchantItem(event.getItem())) {
                //check conflict
                boolean conflict= false;
                for (Enchantment enchantment : event.getEnchantsToAdd().keySet()) {
                    if (registeredEnchantment.conflictsWith(enchantment)) {
                        conflict = true;
                        break;
                    }
                }
                if (conflict) {
                    continue;
                }
                //add enchantment and lore
                event.getItem().addUnsafeEnchantment(registeredEnchantment, 1);
                var lore = event.getItem().lore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add(registeredEnchantment.displayName(-1));
                event.getItem().lore(lore);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (event.getResult() == null) {
            return;
        }
        //add custom enchantments to result
        for (Enchantment registeredEnchantment : registeredEnchantments) {
            if (event.getInventory().getItem(0).containsEnchantment(registeredEnchantment)) {
                event.getResult().addUnsafeEnchantment(registeredEnchantment, 1);
            }
        }
        //check conflicts
        for (Enchantment registeredEnchantment : registeredEnchantments) {
            if (event.getResult().containsEnchantment(Enchantment.getByKey(registeredEnchantment.getKey()))) {
                for (Enchantment enchantment : event.getResult().getEnchantments().keySet()) {
                    if (registeredEnchantment.conflictsWith(enchantment)) {
                        event.setResult(null);
                        return;
                    }
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
        lightningArrow.reload(enchantmentConfig.getConfigurationSection("enchantments.lightning_arrow"));
        explosiveArrow.reload(enchantmentConfig.getConfigurationSection("enchantments.explosive_arrow"));
        trackingArrow.reload(enchantmentConfig.getConfigurationSection("enchantments.tracking_arrow"));
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

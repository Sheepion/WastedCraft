package com.sheepion.wastedcraft.enchant;

import com.sheepion.wastedcraft.WastedCraft;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.enchant
 *
 * @author Sheepion
 * @date 3/2/2022
 */
public abstract class CustomEnchantment extends Enchantment {
    private int priority;
    private String displayName;
    //conflict enchantments
    private final HashSet<String> conflictEnchantments = new HashSet<>();
    //enchant success rate
    private final HashMap<String, Double> enchantSuccessRate = new HashMap<>();
    public CustomEnchantment(@NotNull NamespacedKey key) {
        super(key);
    }
    public int getPriority() {
        return priority;
    }
    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnchanted(ItemStack item) {
        return item.containsEnchantment(Enchantment.getByKey(getKey()));
    }
    /**
     * reload config
     *
     * @param config the lightning arrow config section
     */
    public void reload(ConfigurationSection config) {
        priority = config.getInt("priority");
        displayName = config.getString("display-name");
        //reload conflict enchantments
        conflictEnchantments.clear();
        for (String conflicts : config.getStringList("conflicts")) {
            conflictEnchantments.add(conflicts.toLowerCase());
        }
        for (String conflictEnchantment : conflictEnchantments) {
            WastedCraft.plugin.getLogger().info("conflict enchantments: " + conflictEnchantment);
        }
        //reload success rate
        enchantSuccessRate.clear();
        for (String key : config.getConfigurationSection("success-rate").getKeys(false)) {
            enchantSuccessRate.put(key.toUpperCase(), config.getDouble("success-rate." + key));
        }
        for (String key : enchantSuccessRate.keySet()) {
            WastedCraft.plugin.getLogger().info("enchant success rate: " + key + " " + enchantSuccessRate.get(key));
        }
    }
    @Override
    public @NotNull String getName() {
        return getDisplayName();
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return conflictEnchantments.contains(other.getKey().getKey());
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        //check conflict enchantments
        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            if (conflictsWith(enchantment)) {
                return false;
            }
        }
        //check success rate
        if (enchantSuccessRate.containsKey(item.getType().toString())) {
            return Math.random() < enchantSuccessRate.get(item.getType().toString());
        }
        return false;
    }
    @Override
    public @NotNull Component displayName(int level) {
        if(level!=-1) {
            return Component.text(getName() + " " + level);
        }
        return Component.text(getName());
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return null;
    }

}

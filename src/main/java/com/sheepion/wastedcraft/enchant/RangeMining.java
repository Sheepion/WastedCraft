package com.sheepion.wastedcraft.enchant;

import com.sheepion.wastedcraft.WastedCraft;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.enchant
 *
 * @author Sheepion
 * @date 2/27/2022
 */
public class RangeMining extends Enchantment implements Listener {
    private String displayName;
    //conflict enchantments
    private final HashSet<String> conflictEnchantments = new HashSet<>();
    //enchant success rate
    private final HashMap<String, Double> enchantSuccessRate = new HashMap<>();

    public String getDisplayName() {
        return displayName;
    }

    /**
     * reload config
     *
     * @param config the range mining config section
     */
    public void reload(ConfigurationSection config) {
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.containsEnchantment(Enchantment.getByKey(getKey()))) {
            Location location = event.getBlock().getLocation();
            int[] mx;
            int[] my;
            int[] mz;
            //block under or above player
            if (event.getPlayer().getLocation().getPitch() >= 45
                    || event.getPlayer().getLocation().getPitch() <= -45) {
                mx = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
                my = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
                mz = new int[]{1, 0, -1, 1, -1, 1, 0, -1};
            }
            //block in the north or south
            else if (event.getPlayer().getLocation().getYaw() >= -45 && event.getPlayer().getLocation().getYaw() <= 45
                    || event.getPlayer().getLocation().getYaw() >= 135 || event.getPlayer().getLocation().getYaw() <= -135) {
                mx = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
                my = new int[]{1, 0, -1, 1, -1, 1, 0, -1};
                mz = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
            }
            //block in the east or west
            else {
                mx = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
                my = new int[]{1, 0, -1, 1, -1, 1, 0, -1};
                mz = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
            }
            for (int i = 0; i < 8; i++) {
                Block target = location.clone().add(mx[i], my[i], mz[i]).getBlock();
                //check if the block can be mined by destroy speed.
                if (target.getBreakSpeed(event.getPlayer()) >= event.getBlock().getBreakSpeed(event.getPlayer())) {
                    target.breakNaturally(item);
                    Damageable damageable = (Damageable) event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                    damageable.setDamage(damageable.getDamage() + 1);
                    item.setItemMeta(damageable);
                }
            }
        }
    }

    public RangeMining(@NotNull NamespacedKey key) {
        super(key);
    }

    @Override
    public @NotNull String getName() {
        return "范围采掘";
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
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
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
        WastedCraft.plugin.getLogger().info("check conflict enchantments: " + other.getKey().getKey());
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
        return Component.text(getName() + " " + level);
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

    @Override
    public @NotNull String translationKey() {
        return "wastedcraft.enchant.range_mining";
    }
}

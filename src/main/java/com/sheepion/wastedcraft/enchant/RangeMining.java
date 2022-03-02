package com.sheepion.wastedcraft.enchant;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.enchant
 *
 * @author Sheepion
 * @date 2/27/2022
 */
public class RangeMining extends CustomEnchantment implements Listener {
    public RangeMining(@NotNull NamespacedKey key) {
        super(key);
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

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull String translationKey() {
        return "wastedcraft.enchant.range_mining";
    }
}

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
import org.bukkit.event.Listener;
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
public class LightningArrow extends CustomEnchantment implements Listener {

    public LightningArrow(@NotNull NamespacedKey key) {
        super(key);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getBow()!=null&&event.getBow().containsEnchantment(Enchantment.getByKey(getKey()))) {
            LightningArrowTask task = new LightningArrowTask(event.getProjectile());
            task.setTask(WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin, task, 0, 1));
        }
    }

    @Override
    public @NotNull String translationKey() {
        return "wastedcraft.enchant.lightning_arrow";
    }
}

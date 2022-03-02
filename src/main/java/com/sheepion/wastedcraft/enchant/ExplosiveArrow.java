package com.sheepion.wastedcraft.enchant;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.NotNull;

/**
 * project name: WastedCraft
 * package: com.sheepion.wastedcraft.enchant
 *
 * @author Sheepion
 * @date 3/2/2022
 */
public class ExplosiveArrow extends CustomEnchantment implements Listener {
    public ExplosiveArrow(@NotNull NamespacedKey key) {
        super(key);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getBow()!=null&&event.getBow().containsEnchantment(Enchantment.getByKey(getKey()))) {
            ExplosiveArrowTask task = new ExplosiveArrowTask(event.getProjectile());
            task.setTask(WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin, task, 0, 1));
        }
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public @NotNull String translationKey() {
        return "wastedcraft.enchant.explosive_arrow";
    }
}

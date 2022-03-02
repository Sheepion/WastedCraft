package com.sheepion.wastedcraft.enchant;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.NamespacedKey;
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
public class TrackingArrow extends CustomEnchantment implements Listener {
    public TrackingArrow(@NotNull NamespacedKey key) {
        super(key);
    }
    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getBow()!=null&&isEnchanted(event.getBow())) {
            TrackingArrowTask task = new TrackingArrowTask(event.getProjectile());
            task.setTask(WastedCraft.plugin.getServer().getScheduler().runTaskTimer(WastedCraft.plugin, task, 0, 1));
        }
    }
    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public @NotNull String translationKey() {
        return "wastedcraft.enchant.tracking_arrow";
    }
}

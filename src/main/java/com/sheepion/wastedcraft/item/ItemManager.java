package com.sheepion.wastedcraft.item;

import com.sheepion.custompotionapi.CustomPotionManager;
import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.NamespacedKey;


public class ItemManager {
    public static void registerRecipes() {
        CustomPotionManager.registerPotionEffectType(new TeleportPotionEffectType());
    }
}

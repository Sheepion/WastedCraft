package com.sheepion.wastedcraft.item;

import com.sheepion.wastedcraft.WastedCraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapelessRecipe;

public class ItemManager {
    private static final NamespacedKey TELEPORT_POTION=new NamespacedKey(WastedCraft.plugin, "teleport_potion");
    public static void registerRecipes() {
        if(Bukkit.getRecipe(TELEPORT_POTION)==null){
            //Teleport Potion recipe
            ShapelessRecipe teleportPotionRecipe = new ShapelessRecipe(TELEPORT_POTION, TeleportPotion.getPotion());
            teleportPotionRecipe.addIngredient(1, Material.ENDER_PEARL);
            teleportPotionRecipe.addIngredient(1, Material.POTION);
            Bukkit.getServer().addRecipe(teleportPotionRecipe);
        }
    }
}

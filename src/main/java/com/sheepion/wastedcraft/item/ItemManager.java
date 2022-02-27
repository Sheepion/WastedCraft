package com.sheepion.wastedcraft.item;

import com.sheepion.wastedcraft.WastedCraft;
import io.papermc.paper.potion.PotionMix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;


public class ItemManager {
    private static final NamespacedKey TELEPORT_POTION=new NamespacedKey(WastedCraft.plugin, "teleport_potion");
    public static void registerRecipes() {
        if(Bukkit.getRecipe(TELEPORT_POTION)==null){
            //add brewer recipe
            RecipeChoice input = new RecipeChoice.ExactChoice(new Potion(PotionType.THICK).toItemStack(1));
            RecipeChoice ingredient = new RecipeChoice.MaterialChoice(Material.ENDER_PEARL);
            PotionMix tpPotion = new PotionMix(TELEPORT_POTION, TeleportPotion.getPotion(), input, ingredient);
            WastedCraft.plugin.getServer().getPotionBrewer().addPotionMix(tpPotion);
        }

    }
}

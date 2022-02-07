package com.sheepion.wastedcraft.api;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.UUID;

import static com.sheepion.wastedcraft.WastedCraft.plugin;

/**
 * This class is used to manage thirstiness of players.
 * Thirstiness range is [0,20].
 */
public class Thirstiness {
    private static final NamespacedKey THIRSTINESS_KEY = new NamespacedKey(plugin, "thirstiness");


    /**
     * get player's thirstiness
     *
     * @param player player to get thirstiness of
     * @return player's thirstiness
     */
    public static double getThirstiness(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        //return thirstiness if it exists
        if (container.has(THIRSTINESS_KEY, PersistentDataType.DOUBLE)) {
            return container.get(THIRSTINESS_KEY, PersistentDataType.DOUBLE);
        }
        //create thirstiness if it doesn't exist
        container.set(THIRSTINESS_KEY, PersistentDataType.DOUBLE, 20.0);
        return 20;
    }

    /**
     * set player's thirstiness
     * this will clamp the thirstiness to [0,20]
     *
     * @param player      player to set thirstiness of
     * @param thirstiness thirstiness to set
     */
    public static void setThirstiness(Player player, double thirstiness) {
        if (thirstiness < 0) {
            thirstiness = 0;
        }
        if (thirstiness > 30) {
            thirstiness = 30;
        }
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(THIRSTINESS_KEY, PersistentDataType.DOUBLE, thirstiness);
    }

    /**
     * increase player's thirstiness by amount
     * this will clamp the thirstiness to [0,20], plus 5 saturation.
     *
     * @param player player to increase thirstiness of
     * @param amount amount to increase thirstiness by, can be negative
     * @return player's thirstiness after increase
     */
    public static double increaseThirstiness(Player player, double amount) {
        //refuse to increase thirstiness if thirstiness is full
        if (getThirstiness(player)>=20&&amount>0){
            return getThirstiness(player);
        }
        double newThirstiness = getThirstiness(player) + amount;
        //saturation capped at 5
        setThirstiness(player, newThirstiness);

        if (newThirstiness <= 0) {
            return 0;
        }
        if (newThirstiness >= 30) {
            return 30;
        }
        return newThirstiness;
    }

    /**
     * decrease player's thirstiness by amount
     * this will clamp the thirstiness to [0,20], plus 5 saturation.
     *
     * @param player player to decrease thirstiness of
     * @param amount amount to decrease thirstiness by, can be negative
     * @return player's thirstiness after decrease
     */
    public static double decreaseThirstiness(Player player, double amount) {
        //refuse to increase thirstiness if thirstiness is full
        if (getThirstiness(player)>=20&&amount<0){
            return getThirstiness(player);
        }
        double newThirstiness = getThirstiness(player) - amount;
        setThirstiness(player, newThirstiness);
        if (newThirstiness <= 0) {
            return 0;
        }
        if (newThirstiness >= 30) {
            return 30;
        }
        return newThirstiness;
    }

}

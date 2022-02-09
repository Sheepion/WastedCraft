package com.sheepion.wastedcraft.command;

import com.sheepion.wastedcraft.listener.PetProtector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PetCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (sender instanceof Player) {
            if (args[0].equalsIgnoreCase("listall")) {
                if (!sender.hasPermission("wastedcraft.pet.listall")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                PetProtector.findTamedEntity((Player) sender);
                return true;
            }
        }
        return false;
    }
}

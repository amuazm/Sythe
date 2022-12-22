package me.amuazm.scythehook.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HoeWarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            p.performCommand("summon minecraft:piglin ~ ~ ~ {HandItems:[{Count:1,id:iron_hoe},{}],IsImmuneToZombification:1,ActiveEffects:[{Id:11,Amplifier:9999,Duration:9999}]}");
            p.performCommand("summon minecraft:wither_skeleton ~ ~ ~ {HandItems:[{Count:1,id:iron_hoe},{}],ActiveEffects:[{Id:11,Amplifier:9999,Duration:9999}]}");

        }
        return true;
    }
}

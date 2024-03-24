package me.amuazm.sythe.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HoeMobCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player p) {
      if (args.length == 0) {
        p.performCommand("summon minecraft:husk ~ ~ ~ {HandItems:[{Count:1,id:iron_hoe},{}]}");
      } else if (args.length == 1) {
        String mob = args[0];
        p.performCommand(
            "summon minecraft:" + mob + " ~ ~ ~ {HandItems:[{Count:1,id:iron_hoe},{}]}");
      }
      // TODO: Look into args suggestion of grapplinghook
    }
    return true;
  }
}

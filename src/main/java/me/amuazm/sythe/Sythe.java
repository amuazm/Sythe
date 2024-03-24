package me.amuazm.sythe;

import me.amuazm.sythe.commands.HoeMobCommand;
import me.amuazm.sythe.commands.HoeWarCommand;
import me.amuazm.sythe.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Sythe extends JavaPlugin {
  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(new HoeListener(), this);
    getServer().getPluginManager().registerEvents(new StringListener(this), this);
    getServer().getPluginManager().registerEvents(new ShieldListener(), this);
    getServer().getPluginManager().registerEvents(new ChainListener(this), this);
    getServer().getPluginManager().registerEvents(new RopeListener(this), this);
    getServer().getPluginManager().registerEvents(new PointGunListener(this), this);
    getCommand("hoemob").setExecutor(new HoeMobCommand());
    getCommand("hoewar").setExecutor(new HoeWarCommand());
  }
}

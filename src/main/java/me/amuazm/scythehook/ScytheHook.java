package me.amuazm.scythehook;

import org.bukkit.plugin.java.JavaPlugin;

public final class ScytheHook extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new HitByHoeListener(), this);
    }

}

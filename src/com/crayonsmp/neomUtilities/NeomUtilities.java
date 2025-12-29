package com.crayonsmp.neomUtilities;

import com.crayonsmp.neomUtilities.duralki.DuralkiListener;
import com.crayonsmp.neomUtilities.gauntlet.GauntletListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeomUtilities extends JavaPlugin {
    private static JavaPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new DuralkiListener(), this);
        getServer().getPluginManager().registerEvents(new GauntletListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static JavaPlugin getInstance() {
        return instance;
    }
}

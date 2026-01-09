package com.crayonsmp.neomUtilities;

import com.crayonsmp.neomUtilities.items.biomchanger.BiomChangerListener;
import com.crayonsmp.neomUtilities.items.biomchanger.BiomChangerService;
import com.crayonsmp.neomUtilities.items.duralki.DuralkiListener;
import com.crayonsmp.neomUtilities.items.gauntlet.GauntletListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeomUtilities extends JavaPlugin {
    private static JavaPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        BiomChangerService biomChangerService = new BiomChangerService();
        biomChangerService.loadConfig();

        getServer().getPluginManager().registerEvents(new DuralkiListener(), this);
        getServer().getPluginManager().registerEvents(new GauntletListener(this), this);
        getServer().getPluginManager().registerEvents(new BiomChangerListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static JavaPlugin getInstance() {
        return instance;
    }
}

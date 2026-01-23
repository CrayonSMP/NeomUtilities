package com.crayonsmp.neomUtilities;

import com.crayonsmp.neomUtilities.items.biomchanger.BiomChangerListener;
import com.crayonsmp.neomUtilities.items.biomchanger.BiomChangerService;
import com.crayonsmp.neomUtilities.items.duralki.DuralkiListener;
import com.crayonsmp.neomUtilities.items.gauntlet.GauntletListener;
import com.crayonsmp.neomUtilities.items.hatchableblock.HatchListener;
import com.crayonsmp.neomUtilities.items.hatchableblock.HatchService;
import com.crayonsmp.neomUtilities.utils.ActionService;
import com.crayonsmp.neomUtilities.utils.ConditionService;
import com.crayonsmp.neomUtilities.utils.VariableService;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeomUtilities extends JavaPlugin {
    private static JavaPlugin instance;
    private static ActionService actionService;
    private static ConditionService contextService;
    private static VariableService variableService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        variableService = new VariableService();
        variableService.loadVariables(getConfig());
        contextService = new ConditionService(variableService);
        actionService = new ActionService(contextService, variableService);
        BiomChangerService biomChangerService = new BiomChangerService();
        biomChangerService.loadConfig();

        HatchService.loadAllChunksFromPDC();
        HatchService.startTicking();

        getServer().getPluginManager().registerEvents(new DuralkiListener(), this);
        getServer().getPluginManager().registerEvents(new GauntletListener(this), this);
        getServer().getPluginManager().registerEvents(new BiomChangerListener(), this);
        getServer().getPluginManager().registerEvents(new HatchListener(), this);
    }

    @Override
    public void onDisable() {
        HatchService.saveAllChunksToPDC();
    }

    public static ActionService getActionService() {
        return actionService;
    }

    public static ConditionService getContextService() {
        return contextService;
    }

    public static JavaPlugin getInstance() {
        return instance;
    }
}

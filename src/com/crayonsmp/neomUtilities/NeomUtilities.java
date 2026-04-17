package com.crayonsmp.neomUtilities;

import com.crayonsmp.api.provider.CrayonDefaultProvider;
import com.crayonsmp.neomUtilities.commands.ReloadCommand;
import com.crayonsmp.neomUtilities.items.biomchanger.BiomChangerService;
import com.crayonsmp.neomUtilities.items.duralki.DuralkiListener;
import com.crayonsmp.neomUtilities.items.gauntlet.GauntletListener;
import com.crayonsmp.neomUtilities.items.hatchableblock.HatchListener;
import com.crayonsmp.neomUtilities.items.hatchableblock.HatchService;
import com.crayonsmp.neomUtilities.items.pocket_waystone.PocketWaystoneService;
import com.crayonsmp.neomUtilities.items.trait_sequencer.SequencerService;
import com.crayonsmp.neomUtilities.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class NeomUtilities extends JavaPlugin {
    private static JavaPlugin instance;
    private static ActionService actionService;
    private static ConditionService contextService;
    private static VariableService variableService;
    private static BiomChangerService biomChangerService;
    private static SequencerService sequencerService;
    private static PocketWaystoneService pocketWaystoneService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new CraftEngineListener(), this);
        variableService = new VariableService();
        variableService.loadVariables(getConfig());
        contextService = new ConditionService(variableService);
        actionService = new ActionService(contextService, variableService);

        sequencerService = new SequencerService();
        sequencerService.init(this);

        biomChangerService = new BiomChangerService();
        biomChangerService.init(this);

        if (Bukkit.getPluginManager().isPluginEnabled("CrayonDefault")) {
            pocketWaystoneService = new PocketWaystoneService();
            pocketWaystoneService.init(this);
        } else {
            getLogger().warning("CrayonDefaults not found. All features depand on this plugin will be disabled.");
        }

        HatchService.loadAllChunksFromPDC();
        HatchService.startTicking();

        Objects.requireNonNull(getCommand("neomreload")).setExecutor(new ReloadCommand());

        getServer().getPluginManager().registerEvents(new DuralkiListener(), this);
        getServer().getPluginManager().registerEvents(new GauntletListener(this), this);
        getServer().getPluginManager().registerEvents(new HatchListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
    }

    @Override
    public void onDisable() {
        HatchService.saveAllChunksToPDC();
        for (Player player : Bukkit.getOnlinePlayers()) {
            CrayonDefaultProvider.get().getWaystoneService().removeWaystone(player.getUniqueId().toString());
        }
    }

    public static void reload(){
        NeomUtilities.getInstance().reloadConfig();

        var config = NeomUtilities.getInstance().getConfig();
        NeomUtilities.getVariableService().loadVariables(config);

        NeomUtilities.getBiomChangerService().loadConfig();
        NeomUtilities.getSequencerService().loadRecipes();
        HatchService.reload();
    }


    // In NeomUtilities.java ergänzen:

    public static VariableService getVariableService() {
        return variableService;
    }

    public static BiomChangerService getBiomChangerService() {
        return biomChangerService;
    }

    public static ActionService getActionService() {
        return actionService;
    }

    public static ConditionService getContextService() {
        return contextService;
    }

    public static SequencerService getSequencerService() {return sequencerService;}

    public static JavaPlugin getInstance() {
        return instance;
    }
}

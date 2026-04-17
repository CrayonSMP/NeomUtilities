package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftEngineListener implements Listener {

    @EventHandler
    public void onCraftEngineReload(CraftEngineReloadEvent event) {
        NeomUtilities.reload();
    }
}

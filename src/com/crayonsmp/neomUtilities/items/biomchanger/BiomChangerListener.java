package com.crayonsmp.neomUtilities.items.biomchanger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.awt.event.ActionEvent;

public class BiomChangerListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Action action = event.getAction();
        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)){
            BiomChangerService.trySetCustomBiome(event.getPlayer(), event.getItem());
        }
    }
}

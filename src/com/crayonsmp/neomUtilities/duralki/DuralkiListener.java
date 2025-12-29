package com.crayonsmp.neomUtilities.duralki;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class DuralkiListener implements Listener {

    @EventHandler
    public void onDuralkiUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack duralki = event.getPlayer().getInventory().getItemInOffHand();

        if (duralki != null && DuralkiService.isDuralki(duralki)) {
            if (tool != null && tool.getType().getMaxDurability() > 0) {
                DuralkiService.transferDurability(event.getPlayer(), duralki, tool);
                event.setCancelled(true);
            }
        }
    }
}
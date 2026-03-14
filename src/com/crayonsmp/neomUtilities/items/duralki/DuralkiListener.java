package com.crayonsmp.neomUtilities.items.duralki;

import com.crayonsmp.neomUtilities.NeomUtilities;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
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

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        ItemStack duralki = player.getInventory().getItemInOffHand();

        if (duralki != null && DuralkiService.isDuralki(duralki)) {
            if (tool != null) {

                ItemStack replacement = DuralkiService.getReplacement(tool);
                if (replacement != null && tool.getAmount() <= 1) {
                    player.getInventory().setItemInMainHand(replacement);
                    tool = replacement;
                    player.getInventory().setItemInOffHand(null);
                    duralki = null;

                    player.playSound(player, NeomUtilities.getInstance().getConfig().getString("duralki.replace-sound", "minecraft:block.end_portal_frame.fill"), 1f, 1f);
                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            player.getLocation().add(0, 1.5, 0),
                            20,
                            0, 0, 0,
                            0.2,
                            null,
                            true
                    );
                }

                if (tool.getType().getMaxDurability() > 0) {
                    DuralkiService.transferDurability(player, duralki, tool);
                }
                event.setCancelled(true);
            }
        }
    }
}
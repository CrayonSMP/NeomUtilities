package com.crayonsmp.neomUtilities.items.ender_pouch;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class EnderPouchListener implements Listener {
    private final EnderPouchService enderPouchService;
    public EnderPouchListener(EnderPouchService enderPouchService) {
        this.enderPouchService = enderPouchService;
    }
    @EventHandler
    public void onItemInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        assert item != null;
        if (CraftEngineItems.isCustomItem(item) && Objects.equals(CraftEngineItems.getCustomItemId(item), enderPouchService.getEnderPouchKey())) {
            Player player = e.getPlayer();
            player.openInventory(player.getEnderChest());
            player.playSound(player.getLocation(), enderPouchService.getSound(), enderPouchService.getVolume(), enderPouchService.getPitch());
        }
    }
}

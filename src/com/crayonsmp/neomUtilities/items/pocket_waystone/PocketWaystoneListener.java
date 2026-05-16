package com.crayonsmp.neomUtilities.items.pocket_waystone;

import com.crayonsmp.api.events.WaaystoneGUICloseEvent;
import com.crayonsmp.api.events.WaystoneTeleportEvent;
import com.crayonsmp.api.provider.CrayonDefaultProvider;
import com.crayonsmp.neomUtilities.NeomUtilities;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class PocketWaystoneListener implements Listener {

    private final PocketWaystoneService service;

    public PocketWaystoneListener(PocketWaystoneService service) {
        this.service = service;
    }

    private final HashMap<UUID, ItemStack> playersInGUIs = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && service.isPocketWaystoneItem(item)) {
                if (player.hasCooldown(item.getType())) return;

                e.setCancelled(true);

                String soundName = NeomUtilities.getInstance().getConfig().getString("pocket-waystone.open-sound", "minecraft:item.ender_pearl.throw");
                player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
                player.setCooldown(item.getType(), NeomUtilities.getInstance().getConfig().getInt("pocket-waystone.cooldown", 100));

                CrayonDefaultProvider.get().getWaystoneService().openWaystoneGUI(player, player.getLocation(), "Pocket Waystone GUI");
                playersInGUIs.put(player.getUniqueId(), item);
            }
        }
    }

    @EventHandler
    public void onGUIClose(WaaystoneGUICloseEvent e) {
        playersInGUIs.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(WaystoneTeleportEvent e) {
        Player player = e.getPlayer();
        if (!playersInGUIs.containsKey(player.getUniqueId())) return;

        ItemStack item = playersInGUIs.get(player.getUniqueId());
        playersInGUIs.remove(player.getUniqueId());

        if (item != null && service.isPocketWaystoneItem(item)) {
            applyDamage(player, item);
        }
    }

    private void applyDamage(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (!(meta instanceof Damageable waystoneDamage)) {
            return;
        }

        FileConfiguration config = NeomUtilities.getInstance().getConfig();

        var maxWaystoneDurability = waystoneDamage.getMaxDamage();
        var currentWaystoneDamage = waystoneDamage.getDamage();
        var remainingWaystoneLife = maxWaystoneDurability - currentWaystoneDamage;

        var pointsToDamage = config.getInt("pocket-waystone.durability-loss", 1);

        if (remainingWaystoneLife <= pointsToDamage) {
            item.setAmount(0);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            var newWaystoneDamage = currentWaystoneDamage + pointsToDamage;
            waystoneDamage.setDamage(newWaystoneDamage);
            item.setItemMeta(meta);
        }
    }
}
package com.crayonsmp.neomUtilities.items.uraniumsword;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class UraniumSwordListener implements Listener {

    private final Plugin plugin;
    private final UraniumSwordService service;
    private final NamespacedKey killCountKey;

    public UraniumSwordListener(Plugin plugin, UraniumSwordService service) {
        this.plugin = plugin;
        this.service = service;
        this.killCountKey = new NamespacedKey(plugin, "uranium_sword_kills");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack item = killer.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) return;

        if (service.getUranimDeactivatedSwordKey() == null || service.getUranimActivatedSwordKey() == null) {
            return;
        }

        if (!CraftEngineItems.isCustomItem(item)) {
            return;
        }

        Key itemKey = CraftEngineItems.getCustomItemId(item);
        if (itemKey == null) return;

        if (itemKey.equals(service.getUranimDeactivatedSwordKey())) {
            handleKill(killer, item);
        }
    }

    private void handleKill(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int currentKills = meta.getPersistentDataContainer().getOrDefault(killCountKey, PersistentDataType.INTEGER, 0);
        currentKills++;

        if (currentKills >= service.getActivateKillCount()) {
            meta.getPersistentDataContainer().remove(killCountKey);
            item.setItemMeta(meta);

            replaceItemInHand(player, item, service.getUranimActivatedSwordKey());
            playConfigSound(player, service.getSoundActivate(), service.getVolumeActivate(), service.getPitchActivate());

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    ItemStack currentItem = player.getInventory().getItemInMainHand();
                    if (currentItem != null && CraftEngineItems.isCustomItem(currentItem)) {
                        Key currentKey = CraftEngineItems.getCustomItemId(currentItem);

                        if (currentKey != null && currentKey.equals(service.getUranimActivatedSwordKey())) {
                            replaceItemInHand(player, currentItem, service.getUranimDeactivatedSwordKey());
                            playConfigSound(player, service.getSoundDeactivate(), service.getVolumeDeactivate(), service.getPitchDeactivate());
                        }
                    }
                }
            }.runTaskLater(plugin, service.getDeactivateTimeTicks());

        } else {
            meta.getPersistentDataContainer().set(killCountKey, PersistentDataType.INTEGER, currentKills);
            item.setItemMeta(meta);
        }
    }

    private void replaceItemInHand(Player player, ItemStack oldItem, Key targetKey) {
        if (CraftEngineItems.byId(targetKey) == null) {
            plugin.getLogger().warning("Custom item mit Key '" + targetKey + "' konnte nicht gefunden werden!");
            return;
        }

        ItemStack newItem =  Objects.requireNonNull(CraftEngineItems.byId(targetKey)).buildBukkitItem();
        if (newItem == null) return;

        if (oldItem.hasItemMeta()) {
            ItemMeta oldMeta = oldItem.getItemMeta();
            ItemMeta newMeta = newItem.getItemMeta();

            if (oldMeta != null && newMeta != null) {
                if (oldMeta.hasDisplayName()) newMeta.setDisplayName(oldMeta.getDisplayName());
                if (oldMeta.hasLore()) newMeta.setLore(oldMeta.getLore());

                newMeta.getEnchants().forEach((enchant, level) -> newMeta.removeEnchant(enchant));
                oldMeta.getEnchants().forEach((enchant, level) -> newMeta.addEnchant(enchant, level, true));

                oldMeta.getItemFlags().forEach(newMeta::addItemFlags);
                newMeta.setUnbreakable(oldMeta.isUnbreakable());

                if (oldMeta instanceof Damageable && newMeta instanceof Damageable) {
                    ((Damageable) newMeta).setDamage(((Damageable) oldMeta).getDamage());
                }

                var oldPdc = oldMeta.getPersistentDataContainer();
                var newPdc = newMeta.getPersistentDataContainer();

                for (NamespacedKey key : oldPdc.getKeys()) {
                    if (key.equals(killCountKey)) continue;

                    if (oldPdc.has(key, PersistentDataType.INTEGER)) {
                        newPdc.set(key, PersistentDataType.INTEGER, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.INTEGER)));
                    } else if (oldPdc.has(key, PersistentDataType.STRING)) {
                        newPdc.set(key, PersistentDataType.STRING, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.STRING)));
                    } else if (oldPdc.has(key, PersistentDataType.DOUBLE)) {
                        newPdc.set(key, PersistentDataType.DOUBLE, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.DOUBLE)));
                    } else if (oldPdc.has(key, PersistentDataType.LONG)) {
                        newPdc.set(key, PersistentDataType.LONG, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.LONG)));
                    } else if (oldPdc.has(key, PersistentDataType.BYTE)) {
                        newPdc.set(key, PersistentDataType.BYTE, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.BYTE)));
                    } else if (oldPdc.has(key, PersistentDataType.SHORT)) {
                        newPdc.set(key, PersistentDataType.SHORT, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.SHORT)));
                    } else if (oldPdc.has(key, PersistentDataType.FLOAT)) {
                        newPdc.set(key, PersistentDataType.FLOAT, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.FLOAT)));
                    } else if (oldPdc.has(key, PersistentDataType.BYTE_ARRAY)) {
                        newPdc.set(key, PersistentDataType.BYTE_ARRAY, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.BYTE_ARRAY)));
                    } else if (oldPdc.has(key, PersistentDataType.INTEGER_ARRAY)) {
                        newPdc.set(key, PersistentDataType.INTEGER_ARRAY, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.INTEGER_ARRAY)));
                    } else if (oldPdc.has(key, PersistentDataType.LONG_ARRAY)) {
                        newPdc.set(key, PersistentDataType.LONG_ARRAY, Objects.requireNonNull(oldPdc.get(key, PersistentDataType.LONG_ARRAY)));
                    } else {
                        plugin.getLogger().warning("PDC-Key '" + key + "' konnte nicht kopiert werden (nicht unterstützter Typ).");
                    }
                }

                newItem.setItemMeta(newMeta);
            }
        }

        player.getInventory().setItemInMainHand(newItem);
    }

    private void playConfigSound(Player player, String soundString, float volume, float pitch) {
        try {
            String parsedSound = soundString.toUpperCase().replace("MINECRAFT:", "").replace(".", "_");
            Sound sound = Sound.valueOf(parsedSound);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Sound '" + soundString + "' konnte nicht gefunden werden!");
        }
    }
}
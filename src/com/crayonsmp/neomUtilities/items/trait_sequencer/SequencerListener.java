package com.crayonsmp.neomUtilities.items.trait_sequencer;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SequencerListener implements Listener {

    private final SequncerInventoryService inventoryService = new SequncerInventoryService();
    private final java.util.Map<java.util.UUID, Long> clickDelay = new java.util.HashMap<>();
    private SequencerService sequencerService;
    private static final long DELAY_MS = 200;

    public SequencerListener(SequencerService sequencerService) {
        this.sequencerService = sequencerService;
    }

    @EventHandler
    public void onCustomBlockClick(CustomBlockInteractEvent event) {
        String requiredId = sequencerService.getBlockId();

        if (requiredId == null || requiredId.isEmpty()) return;

        if (event.getPlayer().isSneaking() || event.action().equals(CustomBlockInteractEvent.Action.LEFT_CLICK)) return;

        if (!event.customBlock().id().toString().equals(requiredId)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);
        inventoryService.createSequencerGUI(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!inventoryService.isSequencerInventory(event.getInventory())) return;

        int slot = event.getRawSlot();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getInventory().getItem(SequencerSlots.INPUT) != null && !event.getInventory().getItem(SequencerSlots.INPUT).getType().equals(Material.AIR)) {
                    event.getInventory().setItem(SequencerSlots.CRAFT_BUTTON, sequencerService.getCraftButton(true));
                } else {
                    event.getInventory().setItem(SequencerSlots.CRAFT_BUTTON, sequencerService.getCraftButton(false));
                }
            }
        }.runTaskLater(NeomUtilities.getInstance(), 1L);

        if (event.getClick().isShiftClick() || event.getClick().isKeyboardClick() || event.getClick() == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack currentInSlot = event.getCurrentItem();

        long now = System.currentTimeMillis();
        if (clickDelay.getOrDefault(player.getUniqueId(), 0L) > now) {
            event.setCancelled(true);
            return;
        }
        clickDelay.put(player.getUniqueId(), now + DELAY_MS);

        event.setCancelled(true);

            if (slot == SequencerSlots.CRAFT_BUTTON) {
                inventoryService.tryToCraft(player);
                return;
            }


        for (int i = 0; i < SequencerSlots.MODIFIERS.length; i++) {
            if (slot == SequencerSlots.MODIFIERS[i]) {
                event.setResult(org.bukkit.event.Event.Result.DENY);

                int itemSlotBelow = slot + 9;
                ItemStack itemBelow = event.getInventory().getItem(itemSlotBelow);

                if (currentInSlot == null || currentInSlot.getType() == Material.AIR) return;

                if (itemBelow == null || itemBelow.getType() == Material.AIR) {
                    return;
                }

                ItemMeta meta = currentInSlot.getItemMeta();
                if (meta == null) return;

                Integer currentTier = meta.getPersistentDataContainer().get(
                        SequencerService.TIER_KEY, PersistentDataType.INTEGER);

                if (currentTier == null) currentTier = 1;

                int nextTier = currentTier + 1;
                if (nextTier > 3) nextTier = 1;

                String[] types = {"Structural", "Morphological", "Climatic", "Organic", "Energy", "Kinetic", "Botanical"};

                event.getInventory().setItem(slot, sequencerService.getModifierIcon(types[i], nextTier));
                player.playSound(player.getLocation(), sequencerService.SOUND_BUTTON_CLICK, sequencerService.SOUND_BUTTON_CLICK_VOLUME, sequencerService.SOUND_BUTTON_CLICK_PITCH);

                event.setCancelled(true);
                return;
            }
        }

        int modifierIndex = -1;
        for (int i = 0; i < SequencerSlots.MODIFIERS.length; i++) {
            if (slot == (SequencerSlots.MODIFIERS[i] + 9)) {
                modifierIndex = i;
                break;
            }
        }

        if (modifierIndex != -1) {
            if (cursor != null && cursor.getType() != Material.AIR) {
                if (currentInSlot != null && currentInSlot.getType() != Material.AIR) return;

                ItemStack singleItem = cursor.clone();
                singleItem.setAmount(1);

                if (cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                } else {
                    event.getWhoClicked().setItemOnCursor(null);
                }

                event.setCurrentItem(singleItem);
            } else {
                event.setCancelled(false);
            }

            int finalIndex = modifierIndex;
            int iconSlot = SequencerSlots.MODIFIERS[modifierIndex];
            Bukkit.getScheduler().runTask(NeomUtilities.getInstance(), () -> {
                ItemStack itemNow = event.getInventory().getItem(slot);
                String[] types = {"Structural", "Morphological", "Climatic", "Organic", "Energy", "Kinetic", "Botanical"};

                int newTier = (itemNow != null && itemNow.getType() != Material.AIR) ? 1 : 0;
                event.getInventory().setItem(iconSlot, sequencerService.getModifierIcon(types[finalIndex], newTier));
            });
            return;
        }

        if (slot == SequencerSlots.RESULT) {
            if (cursor == null || cursor.getType() == Material.AIR) event.setCancelled(false);
        } else if (slot == SequencerSlots.INPUT || slot > 53) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!inventoryService.isSequencerInventory(event.getInventory())) return;

        ItemStack input = event.getInventory().getItem(SequencerSlots.INPUT);
        ItemStack resoult = event.getInventory().getItem(SequencerSlots.RESULT);
        List<ItemStack> ModItems = new ArrayList<>();
        for (int modifier : SequencerSlots.MODIFIERS) {
            ModItems.add(event.getInventory().getItem(modifier + 9));
        }
        Player player = (Player) event.getPlayer();
        if (resoult != null) {
            player.getInventory().addItem(resoult);
        }
        if (input != null) {
            player.getInventory().addItem(input);
        }
        if (ModItems.size() > 0) {
            for (ItemStack modItem : ModItems) {
                if (modItem == null) continue;
                if (modItem.getType() == Material.AIR) continue;
                player.getInventory().addItem(modItem);
            }
        }

        event.getInventory().clear();

        inventoryService.removeInventory(event.getInventory());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!inventoryService.isSequencerInventory(event.getInventory())) return;
        event.setCancelled(true);
    }
}
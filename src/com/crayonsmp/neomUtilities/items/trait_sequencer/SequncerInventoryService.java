package com.crayonsmp.neomUtilities.items.trait_sequencer;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.enums.ModifierType;
import com.crayonsmp.neomUtilities.model.Modifier;
import com.crayonsmp.neomUtilities.model.TraitSequence;
import com.crayonsmp.neomUtilities.utils.ChatUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SequncerInventoryService {
    private SequencerService sequencerService = NeomUtilities.getSequencerService();
    private List<Inventory> invs = new ArrayList<>();

    public Inventory createSequencerGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatUtil.format(sequencerService.getInvName()));
        gui.setItem(SequencerSlots.CRAFT_BUTTON, sequencerService.getCraftButton(false));

        String[] types = {"Structural", "Morphological", "Climatic", "Organic", "Energy", "Kinetic", "Botanical"};
        for (int i = 0; i < types.length; i++) {
            gui.setItem(SequencerSlots.MODIFIERS[i], sequencerService.getModifierIcon(types[i], 0));
        }

        invs.add(gui);
        player.openInventory(gui);
        return gui;
    }

    public boolean isSequencerInventory(Inventory inv) {
        return inv != null && invs.contains(inv);
    }

    public boolean tryToCraft(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();

        if (!isSequencerInventory(inv)) {
            return false;
        }

        ItemStack inputItem = inv.getItem(SequencerSlots.INPUT);
        assert inputItem != null;
        ItemStack tempInputItem = new ItemStack(inputItem);
        tempInputItem.removeEnchantments();

        if (tempInputItem == null || tempInputItem.getType() == Material.AIR) {
            return false;
        }

        List<Modifier> activeModifiers = new ArrayList<>();
        List<Integer> usedModItemSlots = new ArrayList<>();

        for (int i = 0; i < SequencerSlots.MODIFIERS.length; i++) {
            int slot = SequencerSlots.MODIFIERS[i];
            ItemStack modifierIcon = inv.getItem(slot);
            ItemStack modifierItem = inv.getItem(slot + 9);

            if (modifierIcon != null && modifierIcon.hasItemMeta()) {
                ItemMeta meta = modifierIcon.getItemMeta();
                Integer tier = meta.getPersistentDataContainer().get(SequencerService.TIER_KEY, PersistentDataType.INTEGER);

                if (tier != null && tier > 0) {
                    if (modifierItem != null && modifierItem.getType() != Material.AIR) {
                        Modifier mod = new Modifier();
                        mod.input = modifierItem.clone();
                        mod.tier = tier;
                        mod.modifierType = ModifierType.values()[i];
                        activeModifiers.add(mod);
                        usedModItemSlots.add(slot + 9);
                    }
                }
            }
        }

        TraitSequence match = sequencerService.findMatchingRecipe(tempInputItem, activeModifiers);

        if (match != null) {
            ItemStack resultTemplate = sequencerService.getResult(match);
            int craftAmount = tempInputItem.getAmount();
            ItemStack currentInResult = inv.getItem(SequencerSlots.RESULT);
            int maxStackSize = resultTemplate.getMaxStackSize();

            if (currentInResult != null && currentInResult.getType() != Material.AIR) {
                if (!currentInResult.isSimilar(resultTemplate)) {
                    return false;
                }
                craftAmount = Math.min(craftAmount, maxStackSize - currentInResult.getAmount());
            } else {
                craftAmount = Math.min(craftAmount, maxStackSize);
            }

            if (craftAmount <= 0) {
                return false;
            }

            ItemStack finalResult = resultTemplate.clone();
            finalResult.setAmount((currentInResult != null ? currentInResult.getAmount() : 0) + craftAmount);

            if (match.isTransferEnchantments) finalResult.addEnchantments(inputItem.getEnchantments());

            inv.setItem(SequencerSlots.RESULT, finalResult);

            tempInputItem.setAmount(tempInputItem.getAmount() - craftAmount);
            inv.setItem(SequencerSlots.INPUT, tempInputItem.getAmount() > 0 ? tempInputItem : null);

            player.playSound(player.getLocation(), sequencerService.SOUND_CRAFTING_SUCCESS, sequencerService.SOUND_CRAFTING_SUCCESS_VOLUME, sequencerService.SOUND_CRAFTING_SUCCESS_PITCH);
            return true;

        } else {
            player.playSound(player.getLocation(), sequencerService.SOUND_CRAFTING_FAILURE, sequencerService.SOUND_CRAFTING_FAILURE_VOLUME, sequencerService.SOUND_CRAFTING_FAILURE_PITCH);
            player.spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);

            inv.setItem(SequencerSlots.INPUT, null);

            for (int slot : usedModItemSlots) {
                inv.setItem(slot, null);
            }

            ItemStack failedItem = sequencerService.getFailedItem();

            if (failedItem != null && failedItem.getType() != Material.AIR) {
                player.getWorld().dropItemNaturally(player.getLocation(), failedItem);
            }

            player.closeInventory();
            return false;
        }
    }

    public void removeInventory(Inventory inv) {
        invs.remove(inv);
    }
}
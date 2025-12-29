package com.crayonsmp.neomUtilities.duralki;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.atomic.AtomicBoolean;

public class DuralkiService {
    private static String itemID = NeomUtilities.getInstance().getConfig().getString("duralki.item-id", "neom:duralki");

    public static boolean isDuralki(ItemStack itemStack) {
        if (!CraftEngineItems.isCustomItem(itemStack)) return false;
        return CraftEngineItems.getCustomItemId(itemStack).toString().equals(itemID);
    }

    public static void transferDurability(Player player, ItemStack duralki, ItemStack tool) {
        if (duralki == null || tool == null) return;
        if (isToolBlacklisted(tool)) return;

        ItemMeta toolMeta = tool.getItemMeta();
        ItemMeta duralkiMeta = duralki.getItemMeta();

        if (!(toolMeta instanceof Damageable toolDamage) || !(duralkiMeta instanceof Damageable duralkiDamage)) {
            return;
        }

        var maxDuralkiDurability = duralkiDamage.getMaxDamage();
        var currentToolDamage = toolDamage.getDamage();
        var currentDuralkiDamage = duralkiDamage.getDamage();
        var remainingDuralkiLife = maxDuralkiDurability - currentDuralkiDamage;

        if (currentToolDamage <= 0) {
            return;
        }

        var pointsToTransfer = Math.min(NeomUtilities.getInstance().getConfig().getInt("duralki.transferrate", 20), currentToolDamage);

        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        if (remainingDuralkiLife <= pointsToTransfer) {
            var finalHeal = Math.max(0, remainingDuralkiLife - 1);
            toolDamage.setDamage(currentToolDamage - finalHeal);
            duralki.setAmount(0);
            player.playSound(player.getLocation(), config.getString("duralki.break-sound", String.valueOf(Sound.ENTITY_ITEM_BREAK)), 1f, 1f);
        } else {
            var newDuralkiDamage = currentDuralkiDamage + pointsToTransfer;
            toolDamage.setDamage(currentToolDamage - pointsToTransfer);
            duralkiDamage.setDamage(newDuralkiDamage);

            duralki.setItemMeta(duralkiMeta);
        }

        tool.setItemMeta(toolMeta);
        player.playSound(player.getLocation(), config.getString("duralki.transfer-sound", String.valueOf(Sound.BLOCK_AMETHYST_BLOCK_CHIME)), 0.5f, 2.0f);
    }

    private static boolean isToolBlacklisted(ItemStack itemStack) {
        if (CraftEngineItems.isCustomItem(itemStack)) {
            return NeomUtilities.getInstance().getConfig().getStringList("duralki.blacklisted-tools").contains(CraftEngineItems.getCustomItemId(itemStack).toString());
        }

        Material type = itemStack.getType();
        return NeomUtilities.getInstance().getConfig().getStringList("duralki.blacklisted-tools").contains(type.getKeyOrNull().toString());
    }
}
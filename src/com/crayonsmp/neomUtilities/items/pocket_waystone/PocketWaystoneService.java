package com.crayonsmp.neomUtilities.items.pocket_waystone;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.Objects;

public class PocketWaystoneService {
    private Key pocketWaystoneKey;

    public void init(Plugin plugin) {
        String id = NeomUtilities.getInstance().getConfig().getString("pocket-waystone.item-id", "neom:pocket_waystone");
        if (CraftEngineItems.byId(Key.from(id)) == null) {
            NeomUtilities.getInstance().getLogger().warning("Pocket Waystone item ID '" + id + "' is invalid! Pocket Waystone features will be disabled.");
            return;
        }
        this.pocketWaystoneKey = Key.from(id);
        plugin.getServer().getPluginManager().registerEvents(new PocketWaystoneListener(this), plugin);
    }

    public boolean isPocketWaystoneItem(ItemStack item) {
        if (item == null || !CraftEngineItems.isCustomItem(item)) return false;
        return Objects.equals(CraftEngineItems.getCustomItemId(item), pocketWaystoneKey);
    }
}
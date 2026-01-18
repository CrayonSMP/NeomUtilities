package com.crayonsmp.neomUtilities.items.hatchableblock;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.bukkit.api.event.FurniturePlaceEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

public class HatchListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        String dataStr = event.getChunk().getPersistentDataContainer().get(HatchService.CHUNK_DATA_KEY, PersistentDataType.STRING);
        if (dataStr == null || dataStr.isEmpty()) return;

        for (String entry : dataStr.split("\\|")) {
            if (entry.isEmpty()) continue;
            String[] p = entry.split(",");
            if (p.length < 5) continue;

            HatchData data = new HatchData(
                    new Location(event.getWorld(), Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2])),
                    p[3],
                    Long.parseLong(p[4])
            );

            HatchService.blockTypes.put(data.location, data.type);
            HatchService.tickingBlocks.put(data.location, data.ticks);
            NeomUtilities.getInstance().getLogger().info("[HatchDebug] Block " + data.type + " bei " + data.location.toVector() + " in RAM geladen.");
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        HatchService.saveChunkToPDC(event.getChunk());
        // RAM aufräumen
        HatchService.tickingBlocks.keySet().removeIf(loc -> loc.getWorld().equals(event.getWorld()) && (loc.getBlockX() >> 4) == event.getChunk().getX() && (loc.getBlockZ() >> 4) == event.getChunk().getZ());
        HatchService.blockTypes.keySet().removeIf(loc -> loc.getWorld().equals(event.getWorld()) && (loc.getBlockX() >> 4) == event.getChunk().getX() && (loc.getBlockZ() >> 4) == event.getChunk().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(CustomBlockPlaceEvent e) {
        register(e.customBlock().id().toString(), e.bukkitBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurniturePlace(FurniturePlaceEvent e) {
        register(e.furniture().id().toString(), e.furniture().location());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (HatchService.tickingBlocks.containsKey(e.getBlock().getLocation())) {
            HatchService.tickingBlocks.remove(e.getBlock().getLocation());
            HatchService.blockTypes.remove(e.getBlock().getLocation());
        }
    }

    private void register(String id, Location loc) {
        if (NeomUtilities.getInstance().getConfig().contains("hatchableblocks." + id)) {
            HatchService.blockTypes.put(loc, id);
            HatchService.tickingBlocks.put(loc, 0L);
        } else {
            NeomUtilities.getInstance().getLogger().warning("[HatchDebug] Registrierung fehlgeschlagen: " + id + " nicht in Config!");
        }
    }
}
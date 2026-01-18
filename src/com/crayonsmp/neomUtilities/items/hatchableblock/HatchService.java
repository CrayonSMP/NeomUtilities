package com.crayonsmp.neomUtilities.items.hatchableblock;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.ActionContext;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HatchService {
    public static final NamespacedKey CHUNK_DATA_KEY = new NamespacedKey(NeomUtilities.getInstance(), "hatch_chunk_data");
    public static final Map<Location, Long> tickingBlocks = new HashMap<>();
    public static final Map<Location, String> blockTypes = new HashMap<>();

    public static void startTicking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tickingBlocks.isEmpty()) return; // Nur loggen, wenn auch was zu tun ist

                Iterator<Map.Entry<Location, Long>> it = tickingBlocks.entrySet().iterator();
                int count = 0;
                while (it.hasNext()) {
                    Map.Entry<Location, Long> entry = it.next();
                    Location loc = entry.getKey();
                    count++;

                    if (loc.getWorld() == null || !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;

                    long newTicks = entry.getValue() + 20;
                    entry.setValue(newTicks);

                    HatchData data = new HatchData(loc, blockTypes.get(loc), newTicks);

                    if (checkGrowth(data)) {
                        blockTypes.remove(loc);
                        it.remove();
                    }
                }
            }
        }.runTaskTimer(NeomUtilities.getInstance(), 20L, 20L);
    }

    public static boolean checkGrowth(HatchData data) {
        ConfigurationSection typeConfig = NeomUtilities.getInstance().getConfig().getConfigurationSection("hatchableblocks." + data.type);
        if (typeConfig == null) {
            NeomUtilities.getInstance().getLogger().warning("[HatchDebug] Kein Config-Eintrag für Typ: " + data.type);
            return false;
        }

        ConfigurationSection stages = typeConfig.getConfigurationSection("points");
        if (stages == null) return false;

        boolean finished = false;
        for (String stageKey : stages.getKeys(false)) {
            ConfigurationSection stageSection = stages.getConfigurationSection(stageKey);
            if (stageSection == null) continue;

            long target = stageSection.getLong("time");
            if (data.ticks >= target && data.ticks < target + 20) {

                ActionContext context = new ActionContext();
                context.setLocation(data.location);
                context.setWorld(data.location.getWorld());
                context.setBlock(data.location.getBlock());

                NeomUtilities.getActionService().executeAllActions(stageSection, context);

                if (stageKey.equals(typeConfig.getString("stagesize"))) finished = true;
            }
        }
        return finished;
    }

    public static void loadAllChunksFromPDC() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk loadedChunk : world.getLoadedChunks()) {
                String dataStr = loadedChunk.getPersistentDataContainer().get(HatchService.CHUNK_DATA_KEY, PersistentDataType.STRING);

                if (dataStr == null || dataStr.isEmpty()) continue;

                for (String entry : dataStr.split("\\|")) {
                    if (entry.isEmpty()) continue;
                    String[] p = entry.split(",");
                    if (p.length < 5) continue;

                    try {
                        HatchData data = new HatchData(
                                new Location(world, Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2])),
                                p[3],
                                Long.parseLong(p[4])
                        );

                        HatchService.blockTypes.put(data.location, data.type);
                        HatchService.tickingBlocks.put(data.location, data.ticks);
                    } catch (NumberFormatException e) {
                        NeomUtilities.getInstance().getLogger().warning("[Hatch] Fehler beim Parsen der Daten in Chunk: " + loadedChunk.getX() + "/" + loadedChunk.getZ());
                    }
                }
            }
        }
    }

    public static void saveAllChunksToPDC() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk loadedChunk : world.getLoadedChunks()) {
                saveChunkToPDC(loadedChunk);
                tickingBlocks.keySet().removeIf(loc -> loc.getWorld().equals(world) && (loc.getBlockX() >> 4) == loadedChunk.getX() && (loc.getBlockZ() >> 4) == loadedChunk.getZ());
                blockTypes.keySet().removeIf(loc -> loc.getWorld().equals(world) && (loc.getBlockX() >> 4) == loadedChunk.getX() && (loc.getBlockZ() >> 4) == loadedChunk.getZ());
            }
        }
    }

    public static void saveChunkToPDC(Chunk chunk) {
        StringBuilder sb = new StringBuilder();
        int found = 0;
        for (Location loc : tickingBlocks.keySet()) {
            if (loc.getWorld().getName().equals(chunk.getWorld().getName()) && (loc.getBlockX() >> 4) == chunk.getX() && (loc.getBlockZ() >> 4) == chunk.getZ()) {
                sb.append(loc.getBlockX()).append(",").append(loc.getBlockY()).append(",").append(loc.getBlockZ()).append(",")
                        .append(blockTypes.get(loc)).append(",").append(tickingBlocks.get(loc)).append("|");
                found++;
            }
        }
        if (sb.length() > 0) {
            chunk.getPersistentDataContainer().set(CHUNK_DATA_KEY, PersistentDataType.STRING, sb.toString());
        } else {
            chunk.getPersistentDataContainer().remove(CHUNK_DATA_KEY);
        }
    }
}
package com.crayonsmp.neomUtilities.items.biomchanger;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BiomChangerService {
    private static final HashMap<String, BiomChangerSettings> itemSettingsMap = new HashMap<>();

    public void loadConfig() {
        itemSettingsMap.clear();

        NeomUtilities.getInstance().reloadConfig();
        var config = NeomUtilities.getInstance().getConfig();

        ConfigurationSection itemsSection = config.getConfigurationSection("biomchanger.items");

        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemData = itemsSection.getConfigurationSection(itemKey);
                if (itemData != null) {
                    String biome = itemData.getString("biom");
                    int radius = itemData.getInt("radius", 10);
                    boolean remove = itemData.getBoolean("removeItems", true);
                    List<String> players = itemData.getStringList("players");

                    itemSettingsMap.put(itemKey, new BiomChangerSettings(biome, radius, remove, players));
                }
            }
        }
    }

    public static boolean isBiomChangerItem(ItemStack itemStack) {
        String id = getCustomId(itemStack);
        return id != null && itemSettingsMap.containsKey(id);
    }

    private static String getCustomId(ItemStack itemStack) {
        if (itemStack == null || !CraftEngineItems.isCustomItem(itemStack)) return null;
        Object id = CraftEngineItems.getCustomItemId(itemStack);
        return id != null ? id.toString() : null;
    }

    public static void trySetCustomBiome(Player player, ItemStack itemStack) {
        String customId = getCustomId(itemStack);
        if (customId == null || !itemSettingsMap.containsKey(customId)) return;

        BiomChangerSettings settings = itemSettingsMap.get(customId);

        if (!settings.allowedPlayers.contains(player.getUniqueId().toString())) {
            return;
        }

        Location loc = player.getLocation();
        World world = player.getWorld();

        world.spawnParticle(Particle.WHITE_ASH, loc, 5000, 4, 4, 4, 0.1);
        world.playSound(loc, Sound.ENTITY_ENDER_EYE_DEATH, 2.0f, 0.0f);

        BlockVector3 center = BukkitAdapter.asBlockVector(loc);

        setCustomBiome(world, center, settings.radius, settings.biomeId);

        if (settings.removeItems) {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    public static void setCustomBiome(org.bukkit.World bukkitWorld, BlockVector3 center, int radius, String biomeId) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bukkitWorld);
        BiomeType customBiome = BiomeType.REGISTRY.get(biomeId);

        if (customBiome != null) {
            BlockVector3 radiusVector = BlockVector3.at(radius, radius, radius);
            EllipsoidRegion region = new EllipsoidRegion(world, center, radiusVector.toVector3());

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                BiomeReplace replace = new BiomeReplace(editSession, customBiome);
                RegionVisitor visitor = new RegionVisitor(region, replace);
                Operations.complete(visitor);
                editSession.flushSession();

                int chunkRadius = (radius >> 4) + 1;
                int centerChunkX = center.x() >> 4;
                int centerChunkZ = center.z() >> 4;

                for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x++) {
                    for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z++) {
                        bukkitWorld.refreshChunk(x, z);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
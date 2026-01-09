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
    private static final HashMap<String, String> itemToBiomeMap = new HashMap<>();
    private static int currentRadius = 10;
    private static List<String> allowedPlayers = new ArrayList<>();
    private static boolean removeItems = true;

    public void loadConfig() {
        itemToBiomeMap.clear();

        NeomUtilities.getInstance().reloadConfig();
        var config = NeomUtilities.getInstance().getConfig();

        currentRadius = config.getInt("biomchanger.radius", 10);
        allowedPlayers = config.getStringList("biomchanger.players");
        removeItems = config.getBoolean("biomchanger.remove-items", true);

        ConfigurationSection section = config.getConfigurationSection("biomchanger.items");

        if (section != null) {
            for (String itemKey : section.getKeys(false)) {
                String biomeValue = section.getString(itemKey);
                if (biomeValue != null) {
                    itemToBiomeMap.put(itemKey, biomeValue);
                }
            }
        }
    }

    public static boolean isBiomChangerItem(ItemStack itemStack) {
        if (!CraftEngineItems.isCustomItem(itemStack)) return itemToBiomeMap.containsKey(itemStack.getType().getKeyOrThrow().toString());
        return itemToBiomeMap.containsKey(Objects.requireNonNull(CraftEngineItems.getCustomItemId(itemStack)).toString());
    }

    public static boolean isAllowedPlayer(Player player) {
        return allowedPlayers.contains(player.getUniqueId().toString());
    }

    public static void trySetCustomBiome(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        Object customIdObj = CraftEngineItems.getCustomItemId(itemStack);
        if (customIdObj == null) return;

        String customId = customIdObj.toString();
        if (!isBiomChangerItem(itemStack)) return;
        if (!isAllowedPlayer(player)) return;

        String biomeId = itemToBiomeMap.get(customId);
        if (biomeId == null) return;

        Location loc = player.getLocation();
        World world = player.getWorld();

        world.spawnParticle(Particle.WHITE_ASH, loc, 1000, 20, 20, 20, 1);
        world.playSound(loc, Sound.ENTITY_ENDER_EYE_DEATH, 2.0f, 0.0f);

        BlockVector3 center = BukkitAdapter.asBlockVector(loc);

        // Biom setzen
        setCustomBiome(world, center, currentRadius, biomeId);

        if (removeItems) {
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

                int blockX = center.x();
                int blockZ = center.z();

                int chunkRadius = (radius >> 4) + 1;
                int centerChunkX = blockX >> 4;
                int centerChunkZ = blockZ >> 4;

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
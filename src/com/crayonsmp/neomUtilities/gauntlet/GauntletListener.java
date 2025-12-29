package com.crayonsmp.neomUtilities.gauntlet;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class GauntletListener implements Listener {

    private final Plugin plugin;
    private final String itemID = NeomUtilities.getInstance().getConfig().getString("gauntlet.item-id", "neom:gauntlet");
    private final NamespacedKey fuel;
    private final Map<Block, BlockFace> interactedBlockFaces = new ConcurrentHashMap<>();

    List<Material> drillableMaterials = List.of(
            Material.STONE,
            Material.COBBLESTONE,
            Material.TUFF,
            Material.INFESTED_STONE,
            Material.GRANITE,
            Material.DIORITE,
            Material.ANDESITE,
            Material.COAL_ORE,
            Material.COPPER_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE,
            Material.INFESTED_DEEPSLATE,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.BLACKSTONE,
            Material.BASALT,
            Material.SMOOTH_BASALT,
            Material.CALCITE,
            Material.AMETHYST_BLOCK,
            Material.BUDDING_AMETHYST,
            Material.BROWN_TERRACOTTA,
            Material.TERRACOTTA,
            Material.WHITE_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.MAGMA_BLOCK,
            Material.CRIMSON_NYLIUM,
            Material.WARPED_NYLIUM,
            Material.NETHERRACK,
            Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.DRIPSTONE_BLOCK,
            Material.SANDSTONE,
            Material.RED_SANDSTONE,
            Material.ICE,
            Material.PACKED_ICE,
            Material.BLUE_ICE,
            Material.RAW_IRON_BLOCK,
            Material.RAW_COPPER_BLOCK,
            Material.RAW_GOLD_BLOCK,
            Material.END_STONE,
            Material.POLISHED_BLACKSTONE_BRICKS,
            Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
            Material.POLISHED_BLACKSTONE,
            Material.CHISELED_POLISHED_BLACKSTONE,
            Material.POLISHED_BASALT,
            Material.PURPUR_BLOCK,
            Material.PURPUR_PILLAR,
            Material.PRISMARINE,
            Material.DARK_PRISMARINE,
            Material.NETHER_BRICKS,
            Material.CHISELED_NETHER_BRICKS,
            Material.GILDED_BLACKSTONE,
            Material.SANDSTONE,
            Material.BRICKS,
            Material.TUFF_BRICKS,
            Material.CHISELED_TUFF,
            Material.POLISHED_TUFF
    );


    public GauntletListener(Plugin plugin) {
        this.plugin = plugin;

        this.fuel = NamespacedKey.fromString("abilities", this.plugin);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(EquipmentSlot.HAND);

        if (!CraftEngineItems.isCustomItem(item)) {
            return;
        }

        if (!CraftEngineItems.getCustomItemId(item).toString().equals(itemID)) {
            return;
        }

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();

        RayTraceResult result = player.getWorld().rayTraceBlocks(eyeLoc, direction, 5.0);

        BlockFace face = BlockFace.SELF;

        if (result != null && result.getHitBlockFace() != null) {
            face = result.getHitBlockFace();
        }

        interactedBlockFaces.put(event.getBlock(), face);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(EquipmentSlot.HAND);

        if (!CraftEngineItems.isCustomItem(item) || !CraftEngineItems.getCustomItemId(item).toString().equals(itemID)) {
            return;
        }

        ItemStack offHand = player.getInventory().getItem(EquipmentSlot.OFF_HAND);

        if (!offHand.getType().equals(Material.COAL) && !offHand.getType().equals(Material.CHARCOAL)) {
            return;
        }


        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        Integer fuel = persistentDataContainer.get(this.fuel, PersistentDataType.INTEGER);

        if (fuel == null) {
            fuel = 0;
        }

        if (fuel > 300) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        if (lore.size() > 1) {
            lore.removeLast();
        }

        lore.add("§7Fuel: " + fuel);
        itemMeta.setLore(lore);

        // IDK what that do.
        //offHand.subtract();
        persistentDataContainer.set(this.fuel, PersistentDataType.INTEGER, fuel += 5);

        item.setItemMeta(itemMeta);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();

        BlockFace blockFace = interactedBlockFaces.remove(block);
        if (blockFace == null) {
            return;
        }

        ItemStack item = player.getInventory().getItem(EquipmentSlot.HAND);

        if (!CraftEngineItems.isCustomItem(item) || !CraftEngineItems.getCustomItemId(item).toString().equals(itemID)) {
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        Integer fuel = persistentDataContainer.get(this.fuel, PersistentDataType.INTEGER);

        if (fuel == null || fuel <= 0) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.removeLast();

        lore.add("§7Fuel: " + (fuel - 1));
        itemMeta.setLore(lore);

        int radius = 1;
        persistentDataContainer.set(this.fuel, PersistentDataType.INTEGER, fuel - 1);
        item.setItemMeta(itemMeta);

        List<Block> blocks = this.getBlocks(block.getLocation(), blockFace, value -> drillableMaterials.contains(value.getType()), radius);

        blocks.remove(block);
        if (blocks.isEmpty()) {
            return;
        }

        for (Block blockToBreak : blocks) {
            blockToBreak.breakNaturally(item);
        }
    }

    private List<Block> getBlocks(Location loc, BlockFace blockFace, Predicate<Block> predicate, int radius) {
        Location start = loc.clone();
        Location end = loc.clone();

        int depth = 0;

        switch (blockFace) {
            case SOUTH, NORTH -> {
                start.add(-radius, -radius, 0);
                end.add(radius, radius, 0);
            }
            case EAST, WEST -> {
                start.add(0, -radius, -radius);
                end.add(0, radius, radius);
            }
            case UP, DOWN -> {
                start.add(-radius, 0, -radius);
                end.add(radius, 0, radius);
            }
        }

        List<Block> blocks = new ArrayList<>();

        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block blockAt = loc.getWorld().getBlockAt(x, y, z);
                    if (predicate.test(blockAt)) {
                        blocks.add(blockAt);
                    }
                }
            }
        }

        return blocks;
    }
}
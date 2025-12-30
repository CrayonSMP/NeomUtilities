package com.crayonsmp.neomUtilities.gauntlet;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class GauntletListener implements Listener {

    private final int CONSUME_PER_BREAK = NeomUtilities.getInstance().getConfig().getInt("gauntlet.mana-consume", 5);
    private final int MANA_PER_PERIOD = NeomUtilities.getInstance().getConfig().getInt("gauntlet.mana-per-period", 1);
    private final String itemID = NeomUtilities.getInstance().getConfig().getString("gauntlet.item-id", "neom:gauntlet");
    private static final NamespacedKey MANA_KEY = new NamespacedKey(NeomUtilities.getInstance(), "mana");
    private static final NamespacedKey BLOCK_BREAK_KEY = new NamespacedKey(NeomUtilities.getInstance(), "breakedblocks");
    private final Map<Block, BlockFace> interactedBlockFaces = new ConcurrentHashMap<>();

    List<Material> drillableMaterials = new ArrayList<>();


    public GauntletListener(Plugin plugin) {
        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        config.getStringList("gauntlet.drillable-blocks").forEach(material -> drillableMaterials.add(Material.getMaterial(material)));

        getManaRegenTask().runTaskTimer(plugin, 0, config.getInt("gauntlet.mana-recharge-period", 20));
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
        Damageable damageable = (Damageable) itemMeta;
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        Integer maxMana = damageable.getMaxDamage();
        Integer mana = persistentDataContainer.get(MANA_KEY, PersistentDataType.INTEGER);
        Integer beaked_blocks = persistentDataContainer.get(BLOCK_BREAK_KEY, PersistentDataType.INTEGER);

        if (mana == null) {
            mana = maxMana;
        }

        if (mana > maxMana) {
            return;
        }

        if (beaked_blocks == null) {
            beaked_blocks = 0;
        }

        int radius = 1;
        persistentDataContainer.set(MANA_KEY, PersistentDataType.INTEGER, mana - CONSUME_PER_BREAK);
        persistentDataContainer.set(BLOCK_BREAK_KEY, PersistentDataType.INTEGER, beaked_blocks + 1);
        damageable.setDamage(maxMana - mana);
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(NeomUtilities.getInstance(), () -> dropGauntlet(player), 1);
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        Bukkit.getScheduler().runTaskLater(NeomUtilities.getInstance(), () -> dropGauntlet(player), 1);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        if (hasPlayerGauntlet(player)) {
            event.setCancelled(true);
            return;
        };
        Bukkit.getScheduler().runTaskLater(NeomUtilities.getInstance(), () -> dropGauntlet(player), 1);
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

    public BukkitRunnable getManaRegenTask(){
        return new BukkitRunnable(){
            @Override
            public void run() {
                FileConfiguration config = NeomUtilities.getInstance().getConfig();
                getItemsInPlayerInventory().forEach(item -> {
                    ItemMeta itemMeta = item.getItemMeta();
                    Damageable damageable = (Damageable) itemMeta;
                    PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                    Integer maxMana = damageable.getMaxDamage();
                    Integer mana = persistentDataContainer.get(MANA_KEY, PersistentDataType.INTEGER);
                    Integer beaked_blocks = persistentDataContainer.get(BLOCK_BREAK_KEY, PersistentDataType.INTEGER);
                    int remove_max_mana = config.getInt("gauntlet.default-mana-capacity-los", 10) + (int) Math.round(config.getDouble("gauntlet.mana-capacity-los-per-break") * beaked_blocks);

                    if (mana == null) {
                        mana = maxMana;
                    }

                    double percent = maxMana * (remove_max_mana / 100.0);
                    int limit = maxMana - (int) Math.round(percent);

                    if (mana >= limit) {
                        return;
                    }

                    persistentDataContainer.set(MANA_KEY, PersistentDataType.INTEGER, mana += MANA_PER_PERIOD);
                    damageable.setDamage(maxMana - mana);

                    item.setItemMeta(itemMeta);
                });
            }
        };
    }

    public List<ItemStack> getItemsInPlayerInventory() {
        List<ItemStack> foundItems = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.getPersistentDataContainer().has(MANA_KEY, PersistentDataType.INTEGER)) {
                        foundItems.add(item);
                    }
                }
            }
        }
        return foundItems;
    }

    public void dropGauntlet(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        boolean foundFirst = false;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null || !CraftEngineItems.isCustomItem(item)) continue;
            if (!CraftEngineItems.getCustomItemId(item).toString().equals(itemID)) continue;

            if (!foundFirst) {
                foundFirst = true;
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.getInventory().setItem(i, null); // Sicherer als .remove()
            }
        }
    }

    public boolean hasPlayerGauntlet(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && CraftEngineItems.isCustomItem(item) && CraftEngineItems.getCustomItemId(item).toString().equals(itemID)) {
                return true;
            }
        }
        return false;
    }
}
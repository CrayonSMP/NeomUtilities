package com.crayonsmp.neomUtilities.items.trait_sequencer;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.enums.ModifierType;
import com.crayonsmp.neomUtilities.model.Modifier;
import com.crayonsmp.neomUtilities.model.TraitSequence;
import com.crayonsmp.neomUtilities.utils.ChatUtil;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SequencerService {
    public List<TraitSequence> recipes = new ArrayList<>();
    public static final NamespacedKey TIER_KEY = new NamespacedKey(NeomUtilities.getInstance(), "sequencer_tier");
    private String blockId;
    private String InvName;
    public String MASSAGE_CRAFTING_SUCCESS;
    public String MASSAGE_CRAFTING_FAILURE;
    public Sound SOUND_BUTTON_CLICK;
    public float SOUND_BUTTON_CLICK_VOLUME;
    public float SOUND_BUTTON_CLICK_PITCH;
    public Sound SOUND_CRAFTING_SUCCESS;
    public float SOUND_CRAFTING_SUCCESS_VOLUME;
    public float SOUND_CRAFTING_SUCCESS_PITCH;
    public Sound SOUND_CRAFTING_FAILURE;
    public float SOUND_CRAFTING_FAILURE_VOLUME;
    public float SOUND_CRAFTING_FAILURE_PITCH;
    private final Logger logger = NeomUtilities.getInstance().getLogger();

    public void init(Plugin plugin) {
        loadRecipes();
        plugin.getServer().getPluginManager().registerEvents(new SequencerListener(), plugin);
    }

    public void loadRecipes() {
        recipes.clear();

        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        this.blockId = config.getString("trait-sequencer.block-id");
        this.InvName = config.getString("trait-sequencer.inv-name");
        MASSAGE_CRAFTING_FAILURE = config.getString("trait-sequencer.massages.crafting-failed");
        MASSAGE_CRAFTING_SUCCESS = config.getString("trait-sequencer.massages.crafting-success");
        SOUND_BUTTON_CLICK = Sound.valueOf(config.getString("trait-sequencer.sounds.button", "ENTITY_ITEM_PICKUP").toUpperCase());
        SOUND_CRAFTING_FAILURE = Sound.valueOf(config.getString("trait-sequencer.sounds.fail", "ENTITY_ITEM_BREAK").toUpperCase());
        SOUND_CRAFTING_SUCCESS = Sound.valueOf(config.getString("trait-sequencer.sounds.success", "ENTITY_PLAYER_LEVELUP").toUpperCase());
        SOUND_BUTTON_CLICK_VOLUME = config.getLong("trait-sequencer.sounds.button-volume", 1L);
        SOUND_BUTTON_CLICK_PITCH = config.getLong("trait-sequencer.sounds.button-pitch", 1L);
        SOUND_CRAFTING_SUCCESS_VOLUME = config.getLong("    trait-sequencer.sounds.success-volume", 1L);
        SOUND_CRAFTING_SUCCESS_PITCH = config.getLong("trait-sequencer.sounds.success-pitch", 1L);
        SOUND_CRAFTING_FAILURE_VOLUME = config.getLong("trait-sequencer.sounds.fail-volume", 1L);
        SOUND_CRAFTING_FAILURE_PITCH = config.getLong("trait-sequencer.sounds.fail-pitch", 1L);
        List<?> recipeList = config.getList("trait-sequencer.recipes");

        if (recipeList == null) {
            logger.warning("[Sequencer] No recipes found in config.yml! Check 'trait-sequencer.recipes'.");
            return;
        }

        for (int i = 0; i < recipeList.size(); i++) {
            Object obj = recipeList.get(i);
            if (!(obj instanceof Map)) {
                logger.warning("[Sequencer] Recipe #" + i + " is not a valid configuration section (Map). Skipping.");
                continue;
            }

            Map<?, ?> recipeMap = (Map<?, ?>) obj;
            TraitSequence sequence = new TraitSequence();

            try {
                if (recipeMap.containsKey("result")) {
                    sequence.Resoult = buildItem((Map<?, ?>) recipeMap.get("result"));
                } else {
                    logger.warning("[Sequencer] Recipe #" + i + " is missing a 'result' item!");
                }

                if (recipeMap.containsKey("input")) {
                    sequence.Input = buildItem((Map<?, ?>) recipeMap.get("input"));
                } else {
                    logger.warning("[Sequencer] Recipe #" + i + " is missing an 'input' item!");
                }

                sequence.modifiers = new ArrayList<>();
                if (recipeMap.containsKey("modifiers")) {
                    List<?> modifierList = (List<?>) recipeMap.get("modifiers");
                    for (int j = 0; j < modifierList.size(); j++) {
                        Map<?, ?> modMap = (Map<?, ?>) modifierList.get(j);
                        Modifier modifier = new Modifier();

                        if (modMap.containsKey("tier")) {
                            modifier.tier = ((Number) modMap.get("tier")).intValue();
                        } else {
                            logger.warning("[Sequencer] Recipe #" + i + ", Modifier #" + j + " is missing 'tier'!");
                        }

                        String modMatName = (String) modMap.get("input");
                        if (modMatName != null) {
                            Material mat = Material.matchMaterial(modMatName.replace("minecraft:", "").toUpperCase());
                            if (mat != null) {
                                modifier.input = new ItemStack(mat);
                            } else {
                                logger.warning("[Sequencer] Recipe #" + i + ", Modifier #" + j + ": Invalid material '" + modMatName + "'");
                            }
                        }

                        String typeStr = (String) modMap.get("type");
                        if (typeStr != null) {
                            try {
                                modifier.modifierType = ModifierType.valueOf(typeStr.trim().toUpperCase());
                            } catch (IllegalArgumentException e) {
                                logger.warning("[Sequencer] Recipe #" + i + ", Modifier #" + j + ": INVALID TYPE '" + typeStr + "'!");
                                logger.warning("Allowed types: STRUCTURAL, MORPHOLOGICAL, CLIMATIC, ORGANIC, ENERGY, KINETIC, BOTANICAL");
                                continue;
                            }
                        } else {
                            logger.warning("[Sequencer] Recipe #" + i + ", Modifier #" + j + " is missing 'type'!");
                        }

                        sequence.modifiers.add(modifier);
                    }
                }

                if (sequence.Input != null && sequence.Resoult != null) {
                    recipes.add(sequence);
                } else {
                    logger.severe("[Sequencer] Recipe #" + i + " could not be loaded due to missing input or result.");
                }

            } catch (Exception e) {
                logger.severe("[Sequencer] Critical error loading recipe #" + i + ": " + e.getMessage());
            }
        }
        logger.info("[Sequencer] Successfully loaded " + recipes.size() + " recipes.");
    }

    private ItemStack buildItem(Map<?, ?> data) {
        if (data.containsKey("id")) {
            String customId = (String) data.get("id");
            try {
                ItemStack item = CraftEngineItems.byId(Key.from(customId)).buildItemStack();
                if (data.containsKey("amount")) {
                    item.setAmount(((Number) data.get("amount")).intValue());
                }
                return item;
            } catch (Exception e) {
                logger.warning("[Sequencer] Custom Item ID not found: " + customId);
            }
        }

        String matName = (String) data.get("material");
        if (matName == null) {
            logger.warning("[Sequencer] Item data missing both 'id' and 'material' fields!");
            return null;
        }

        try {
            Material mat = Material.matchMaterial(matName.replace("minecraft:", "").toUpperCase());
            if (mat == null) {
                logger.warning("[Sequencer] Unknown Minecraft material: " + matName);
                return null;
            }
            int amount = data.containsKey("amount") ? ((Number) data.get("amount")).intValue() : 1;
            return new ItemStack(mat, amount);
        } catch (Exception e) {
            logger.warning("[Sequencer] Error building vanilla item for: " + matName);
            return null;
        }
    }

    public TraitSequence findMatchingRecipe(ItemStack input, List<Modifier> activeModifiers) {
        if (input == null || input.getType() == Material.AIR) return null;

        for (TraitSequence recipe : recipes) {
            if (!recipe.Input.isSimilar(input)) continue;

            if (recipe.modifiers.size() != activeModifiers.size()) continue;

            if (areModifiersMatching(recipe.modifiers, activeModifiers)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean areModifiersMatching(List<Modifier> recipeMods, List<Modifier> userMods) {
        if (recipeMods.size() != userMods.size()) return false;

        List<Modifier> remainingUserMods = new ArrayList<>(userMods);

        for (Modifier recipeMod : recipeMods) {
            boolean foundMatch = false;

            for (int i = 0; i < remainingUserMods.size(); i++) {
                Modifier userMod = remainingUserMods.get(i);

                if (userMod.modifierType == recipeMod.modifierType &&
                        userMod.tier == recipeMod.tier &&
                        userMod.input.getType() == recipeMod.input.getType()) {

                    remainingUserMods.remove(i);
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) return false;
        }

        return remainingUserMods.isEmpty();
    }

    public ItemStack getResult(TraitSequence recipe) {
        if (recipe == null || recipe.Resoult == null) return null;
        return recipe.Resoult.clone();
    }

    public ItemStack getCraftButton(boolean isActivated) {
        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        String pathActivated = "trait-sequencer.craft-activated-button";
        String pathDeactivated = "trait-sequencer.craft-deactivated-button";

        if (isActivated) {
            if (config.isString(pathActivated)) {
                String id = config.getString(pathActivated);
                try {
                    return CraftEngineItems.byId(Key.from(id)).buildItemStack();
                } catch (Exception e) {
                    return new ItemStack(Material.ANVIL);
                }
            }
            return new ItemStack(Material.ANVIL);
        }

        if (config.isString(pathActivated)) {
            String id = config.getString(pathDeactivated);
            try {
                return CraftEngineItems.byId(Key.from(id)).buildItemStack();
            } catch (Exception e) {
                return new ItemStack(Material.ANVIL);
            }
        }
        return new ItemStack(Material.ANVIL);
    }

    public ItemStack getTierItem(int tier) {
        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        String path = "trait-sequencer.tiers." + tier + ".id";
        String id = config.getString(path);

        if (id == null) {
            logger.warning("[Sequencer] Configuration missing for Tier " + tier + " at path: " + path);
            return new ItemStack(Material.BARRIER);
        }

        try {
            if (id.startsWith("minecraft:")) {
                Material mat = Material.matchMaterial(id.replace("minecraft:", "").toUpperCase());
                return (mat != null) ? new ItemStack(mat) : new ItemStack(Material.BARRIER);
            } else {
                return CraftEngineItems.byId(Key.from(id)).buildItemStack();
            }
        } catch (Exception e) {
            logger.warning("[Sequencer] Could not load item for Tier " + tier + " (ID: " + id + ")");
            return new ItemStack(Material.BARRIER);
        }
    }

    public ItemStack getModifierIcon(String type, int tier) {
        ItemStack item = getTierItem(tier);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            FileConfiguration config = NeomUtilities.getInstance().getConfig();

            String display = ChatUtil.format(config.getString("trait-sequencer.modifiers." + type + ".Display", type));
            List<String> lore = config.getStringList("trait-sequencer.modifiers." + type + ".lore");
            String tierDisplay = ChatUtil.format(config.getString("trait-sequencer.tiers." + tier + ".Display", "Tier " + tier));

            lore.forEach(line -> {
                if (line.contains("{tier}")) {
                    line = line.replace("{tier}", tierDisplay);
                }
                line = ChatUtil.format(line);
            });

            meta.setDisplayName(display);
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(TIER_KEY, PersistentDataType.INTEGER, tier);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getFailedItem() {
        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        String path = "trait-sequencer.faild-item";

        if (config.isString(path)) {
            String id = config.getString(path);
            try {
                if (id.startsWith("minecraft:")) {
                    return new ItemStack(Material.valueOf(id.replace("minecraft:", "").toUpperCase()));
                }
                return CraftEngineItems.byId(Key.from(id)).buildItemStack();
            } catch (Exception e) {
                return new ItemStack(Material.COAL);
            }
        }
        return new ItemStack(Material.BARRIER);
    }

    public String getBlockId() {
        return blockId;
    }

    public String getInvName() {
        return InvName;
    }
}

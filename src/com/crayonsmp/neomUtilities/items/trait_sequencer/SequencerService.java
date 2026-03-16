package com.crayonsmp.neomUtilities.items.trait_sequencer;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.enums.ModifierType;
import com.crayonsmp.neomUtilities.model.Modifier;
import com.crayonsmp.neomUtilities.model.TraitSequence;
import com.crayonsmp.neomUtilities.utils.ChatUtil;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SequencerService {
    public List<TraitSequence> recipes = new ArrayList<>();
    public static final NamespacedKey TIER_KEY = new NamespacedKey(NeomUtilities.getInstance(), "sequencer_tier");
    private String blockId;
    private String InvName;

    public void init(Plugin plugin) {
        loadRecipes();
        plugin.getServer().getPluginManager().registerEvents(new SequencerListener(), plugin);
    }

    public void loadRecipes() {
        recipes.clear();
        Bukkit.getLogger().info("§e[Sequencer-Debug] Starte Laden der Rezepte...");

        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        this.blockId = config.getString("trait-sequencer.block-id");
        this.InvName = config.getString("trait-sequencer.inv-name");
        List<?> recipeList = config.getList("trait-sequencer.recipes");

        if (recipeList == null) {
            Bukkit.getLogger().warning("§c[Sequencer-Debug] FEHLER: Pfad 'trait-sequencer.recipes' nicht in Config gefunden oder leer!");
            return;
        }

        Bukkit.getLogger().info("§e[Sequencer-Debug] " + recipeList.size() + " Einträge in der Liste gefunden.");

        for (int i = 0; i < recipeList.size(); i++) {
            Object obj = recipeList.get(i);
            if (!(obj instanceof Map)) {
                Bukkit.getLogger().warning("§c[Sequencer-Debug] Rezept #" + i + " ist kein gültiges Map-Objekt!");
                continue;
            }

            Map<?, ?> recipeMap = (Map<?, ?>) obj;
            TraitSequence sequence = new TraitSequence();

            try {
                // RESULT LADEN
                if (recipeMap.containsKey("result")) {
                    Map<?, ?> resultData = (Map<?, ?>) recipeMap.get("result");
                    sequence.Resoult = buildItem(resultData);
                    if (sequence.Resoult == null) Bukkit.getLogger().warning("§c[Sequencer-Debug] Rezept #" + i + ": Result-Item konnte nicht gebaut werden!");
                }

                // INPUT LADEN
                if (recipeMap.containsKey("input")) {
                    Map<?, ?> inputData = (Map<?, ?>) recipeMap.get("input");
                    sequence.Input = buildItem(inputData);
                    if (sequence.Input == null) Bukkit.getLogger().warning("§c[Sequencer-Debug] Rezept #" + i + ": Input-Item konnte nicht gebaut werden!");
                }

                // MODIFIKATOREN LADEN
                sequence.modifiers = new ArrayList<>();
                if (recipeMap.containsKey("modifiers")) {
                    List<?> modifierList = (List<?>) recipeMap.get("modifiers");
                    for (Object modObj : modifierList) {
                        Map<?, ?> modMap = (Map<?, ?>) modObj;
                        Modifier modifier = new Modifier();

                        // Tier sicher laden (Yaml interpretiert Zahlen oft als Integer)
                        modifier.tier = ((Number) modMap.get("tier")).intValue();

                        // Material sicher laden
                        String modMatName = (String) modMap.get("input");
                        Material mat = Material.matchMaterial(modMatName.replace("minecraft:", "").toUpperCase());
                        if (mat != null) {
                            modifier.input = new ItemStack(mat);
                        } else {
                            Bukkit.getLogger().warning("§c[Sequencer-Debug] Rezept #" + i + ": Material '" + modMatName + "' ist unbekannt!");
                        }

                        // Typ sicher laden
                        String typeStr = (String) modMap.get("type");
                        if (typeStr != null) {
                            try {
                                modifier.modifierType = ModifierType.valueOf(typeStr.trim().toUpperCase());
                            } catch (IllegalArgumentException e) {
                                Bukkit.getLogger().warning("§c[Sequencer-Debug] Rezept #" + i + ": UNGÜLTIGER TYP '" + typeStr + "'!");
                                Bukkit.getLogger().warning("§cErlaubte Typen sind: STRUCTURAL, MORPHOLOGICAL, CLIMATIC, ORGANIC, ENERGY, KINETIC, BOTANICAL");
                            }
                        }
                        modifier.modifierType = ModifierType.valueOf(typeStr.toUpperCase());

                        sequence.modifiers.add(modifier);
                        Bukkit.getLogger().info("§a[Sequencer-Debug] Rezept #" + i + ": Modifikator geladen (" + typeStr + " Tier " + modifier.tier + ")");
                    }
                }

                if (sequence.Input != null && sequence.Resoult != null) {
                    recipes.add(sequence);
                    Bukkit.getLogger().info("§2[Sequencer-Debug] Rezept #" + i + " erfolgreich geladen! (Input: " + sequence.Input.getType() + ")");
                }

            } catch (Exception e) {
                Bukkit.getLogger().warning("§c[Sequencer-Debug] Fehler beim Laden von Rezept #" + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        Bukkit.getLogger().info("§e[Sequencer-Debug] Laden beendet. Gesamtanzahl Rezepte: " + recipes.size());
    }

    private ItemStack buildItem(Map<?, ?> data) {
        if (data.containsKey("id")) {
            String customId = (String) data.get("id");
            try {
                return CraftEngineItems.byId(Key.from(customId)).buildItemStack();
            } catch (Exception e) {
                NeomUtilities.getInstance().getLogger().warning("Custom Item nicht gefunden: " + customId);
            }
        }

        String matName = (String) data.get("material");
        if (matName == null) return null;

        try {
            Material mat = Material.valueOf(matName.replace("minecraft:", "").toUpperCase());
            int amount = (int) data.get("amount");
            return new ItemStack(mat, amount);
        } catch (IllegalArgumentException e) {
            NeomUtilities.getInstance().getLogger().severe("Ungültiges Material in der Config: " + matName);
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

    public ItemStack getCraftButton() {
        FileConfiguration config = NeomUtilities.getInstance().getConfig();
        String path = "trait-sequencer.craft-button";

        if (config.isString(path)) {
            String id = config.getString(path);
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
        String id = config.getString(path, "minecraft:barrier");

        Bukkit.getLogger().info("[Sequencer] Lade Material für Tier " + tier + ": " + id);

        try {
            if (id.startsWith("minecraft:")) {
                Material mat = Material.matchMaterial(id.replace("minecraft:", "").toUpperCase());
                if (mat == null) {
                    return new ItemStack(Material.BARRIER);
                }
                return new ItemStack(mat);
            } else {
                return CraftEngineItems.byId(Key.from(id)).buildItemStack();
            }
        } catch (Exception e) {
            return new ItemStack(Material.BARRIER);
        }
    }

    public ItemStack getModifierIcon(String type, int tier) {
        // Hier wird das Item nun jedes Mal NEU aus der Config-ID erstellt
        ItemStack item = getTierItem(tier);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            FileConfiguration config = NeomUtilities.getInstance().getConfig();

            String display = ChatUtil.format(config.getString("trait-sequencer.modifiers." + type + ".Display", type));
            String lore = ChatUtil.format(config.getString("trait-sequencer.modifiers." + type + ".lore", ""));
            String tierDisplay = ChatUtil.format(config.getString("trait-sequencer.tiers." + tier + ".Display", "Tier " + tier));

            meta.setDisplayName(display);
            List<String> loreList = new ArrayList<>();
            loreList.add(lore);
            loreList.add("");
            loreList.add(tierDisplay);
            meta.setLore(loreList);

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

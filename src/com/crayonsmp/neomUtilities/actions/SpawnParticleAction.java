package com.crayonsmp.neomUtilities.actions;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class SpawnParticleAction extends Action {

    public SpawnParticleAction() {
        id = "particle";
    }

    @Override
    public void execute(ActionContext context, ConfigurationSection config) {
        Location location = context.getLocation();

        if (location == null && context.getPlayer() != null) {
            location = context.getPlayer().getLocation();
        }

        if (location == null) {
            NeomUtilities.getInstance().getLogger().warning("Tried to play particle, but location was null!");
            return;
        }

        String idString = config.getString("id");
        if (idString == null) return;

        Particle particle;
        Object data = null;

        // 1. Basistyp extrahieren (alles vor der Klammer)
        String baseId = idString.contains("{") ? idString.split("\\{")[0] : idString;
        particle = getParticleFromId(baseId);

        if (particle == null) {
            NeomUtilities.getInstance().getLogger().warning("[NeomUtilities] Invalid particle id: " + idString);
            return;
        }

        // 2. Daten-Parsing basierend auf dem Typ
        try {
            if (idString.contains("{")) {
                // FALL A: Block Daten (minecraft:block, minecraft:falling_dust, etc.)
                if (idString.contains("block_state:\"")) {
                    String blockPart = idString.split("block_state:\"")[1].split("\"")[0];
                    data = Bukkit.createBlockData(blockPart);
                }
                // FALL B: Item Daten (minecraft:item)
                else if (idString.contains("item:\"")) {
                    String itemPart = idString.split("item:\"")[1].split("\"")[0];
                    Material mat = Material.matchMaterial(itemPart);
                    if (mat != null) data = new ItemStack(mat);
                }
                // FALL C: Dust / RGB Daten (minecraft:dust)
                // Format: minecraft:dust{color:[1.0, 0.0, 0.0], scale:1.0}
                else if (idString.contains("color:[")) {
                    String colorPart = idString.split("color:\\[")[1].split("\\]")[0];
                    String[] rgb = colorPart.split(",");
                    float r = Float.parseFloat(rgb[0].trim());
                    float g = Float.parseFloat(rgb[1].trim());
                    float b = Float.parseFloat(rgb[2].trim());
                    float size = 1.0f;
                    if (idString.contains("scale:")) {
                        size = Float.parseFloat(idString.split("scale:")[1].split("}")[0].replace("f", "").trim());
                    }
                    data = new Particle.DustOptions(Color.fromRGB((int)(r*255), (int)(g*255), (int)(b*255)), size);
                }
            }
        } catch (Exception e) {
            NeomUtilities.getInstance().getLogger().warning("Error parsing particle data for: " + idString);
        }

        // 3. Spawnen
        int count = config.getInt("count", 1);
        double offsetX = config.getDouble("offset-x", 0);
        double offsetY = config.getDouble("offset-y", 0);
        double offsetZ = config.getDouble("offset-z", 0);
        double speed = config.getDouble("speed", 0);

        Location spawnLoc = config.getBoolean("centered", false) ? location.clone().add(0.5, 0.5, 0.5) : location;

        spawnLoc.getWorld().spawnParticle(particle, spawnLoc, count, offsetX, offsetY, offsetZ, speed, data);
    }

    private Particle getParticleFromId(String id) {
        NamespacedKey key = NamespacedKey.fromString(id.toLowerCase());
        return key != null ? Registry.PARTICLE_TYPE.get(key) : null;
    }
}
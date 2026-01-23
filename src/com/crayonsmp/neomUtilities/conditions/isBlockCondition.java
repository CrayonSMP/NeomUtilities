package com.crayonsmp.neomUtilities.conditions;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.Condition;
import com.crayonsmp.neomUtilities.model.ConditionContext;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class isBlockCondition extends Condition {

    public isBlockCondition(String id) {
        super(id);
    }

    @Override
    public boolean test(ConditionContext context, ConfigurationSection config) {
        String type = config.getString("type");
        String id = config.getString("id");
        String xs = config.getString("x");
        String ys = config.getString("y");
        String zs = config.getString("z");

        if (type == null || id == null || xs == null || ys == null || zs == null || context.getLocation() == null) {
            return false;
        }

        Location baseLoc = context.getLocation();
        double finalX = parseCoordinate(xs, baseLoc.getX());
        double finalY = parseCoordinate(ys, baseLoc.getY());
        double finalZ = parseCoordinate(zs, baseLoc.getZ());

        Location checkLoc = new Location(baseLoc.getWorld(), finalX, finalY, finalZ);

        if (type.equalsIgnoreCase("customblock")) {
            if (CraftEngineBlocks.byId(Key.from(id)) == null){
                NeomUtilities.getInstance().getLogger().warning("Tried to change block, but custom block was invalid!");
                return false;
            }

            return CraftEngineBlocks.getCustomBlockState(checkLoc.getBlock()).owner().value().id().equals(id);
        }

        if (type.equalsIgnoreCase("vanilla")) {
            Material targetMaterial = Material.matchMaterial(id);
            if (targetMaterial == null) return false;


            return checkLoc.getBlock().getType() == targetMaterial;
        }

        return false;
    }

    private double parseCoordinate(String input, double baseValue) {
        if (input.startsWith("~")) {
            if (input.length() == 1) return baseValue;

            try {
                return baseValue + Double.parseDouble(input.substring(1));
            } catch (NumberFormatException e) {
                return baseValue;
            }
        }

        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return baseValue;
        }
    }
}
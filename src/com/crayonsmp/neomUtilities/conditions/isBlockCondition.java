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

        if (type == null || id == null || xs == null || ys == null || zs == null) {
            NeomUtilities.getInstance().getLogger().warning("[Debug] Condition 'is-block' failed: Missing parameters in config or context location is null!");
            return false;
        }

        if (context.getLocation() == null){
            NeomUtilities.getInstance().getLogger().warning("[Debug] context location is null!");
        }

        Location baseLoc = context.getLocation();
        double finalX = parseCoordinate(xs, baseLoc.getX());
        double finalY = parseCoordinate(ys, baseLoc.getY());
        double finalZ = parseCoordinate(zs, baseLoc.getZ());

        Location checkLoc = new Location(baseLoc.getWorld(), finalX, finalY, finalZ);

        if (type.equalsIgnoreCase("customblock")) {
            if (CraftEngineBlocks.byId(Key.from(id)) == null) {
                return false;
            }

            if (!CraftEngineBlocks.isCustomBlock(checkLoc.getBlock())) {
                return false;
            }

            String blockId = CraftEngineBlocks.getCustomBlockState(checkLoc.getBlock()).owner().value().id().toString();

            return blockId.equalsIgnoreCase(id);
        }

        if (type.equalsIgnoreCase("vanilla") || type.equalsIgnoreCase("block")) {
            Material targetMaterial = Material.matchMaterial(id);
            if (targetMaterial == null) {
                return false;
            }

            Material actualMaterial = checkLoc.getBlock().getType();
            return (actualMaterial == targetMaterial);
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
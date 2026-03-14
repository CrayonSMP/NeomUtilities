package com.crayonsmp.neomUtilities.actions;

import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class RemoveBlockAction extends Action {

    public RemoveBlockAction(String id) {
        super(id);
    }

    @Override
    public void execute(ActionContext context, ConfigurationSection config) {
        if (context.getLocation() == null) return;

        Location location = context.getLocation();

        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
            if (CraftEngineFurniture.isFurniture(entity)) {
                CraftEngineFurniture.remove(entity);
                break;
            }
        }

        location.getBlock().setType(Material.AIR);
    }
}

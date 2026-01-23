package com.crayonsmp.neomUtilities.actions;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class ChangeBlockAction extends Action {

    public ChangeBlockAction(String id) {
        super(id);
    }

    @Override
    public void execute(ActionContext context, ConfigurationSection config) {
        Location location = context.getLocation();

        String type = config.getString("type");
        String id = config.getString("id");

        if (type == null){
            NeomUtilities.getInstance().getLogger().warning("Tried to change block, but block type was null!");
            return;
        }
        if (id == null){
            NeomUtilities.getInstance().getLogger().warning("Tried to change block, but block id was null!");
            return;
        }
        if (location == null){
            NeomUtilities.getInstance().getLogger().warning("Tried to change block, but location was null!");
            return;
        }

        type = type.toLowerCase();
        id = id.toLowerCase();


        if (type.equalsIgnoreCase("furniture")){
            if (CraftEngineFurniture.byId(Key.from(id)) == null){
                NeomUtilities.getInstance().getLogger().warning("Tried to change block, but furniture was invalid!");
                return;
            }

            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
                if (CraftEngineFurniture.isFurniture(entity)) {
                    CraftEngineFurniture.remove(entity);
                    break;
                }
            }
            location.getBlock().setType(Material.AIR);
            Location fixedLocation = location.clone().add(0.5, 0, 0.5);
            CraftEngineFurniture.place(fixedLocation, Key.from(id));
            return;
        }

        if (type.equalsIgnoreCase("customblock")){
            if (CraftEngineBlocks.byId(Key.from(id)) == null){
                NeomUtilities.getInstance().getLogger().warning("Tried to change block, but custom block ’" + id + "’  was invalid!");
                return;
            }

            CraftEngineBlocks.place(location, Key.from(id), false);
            return;
        }

        if (type.equalsIgnoreCase("vanilla")){
            if (Material.matchMaterial(id) == null){
                NeomUtilities.getInstance().getLogger().warning("Tried to change block, but block ’" + id + "’ was invalid!");

                return;
            }

            location.getBlock().setType(Material.matchMaterial(id));
            return;
        }

        NeomUtilities.getInstance().getLogger().warning("Tried to change block, but type was invalid!");
    }
}

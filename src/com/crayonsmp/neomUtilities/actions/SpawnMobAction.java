package com.crayonsmp.neomUtilities.actions;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.Optional;

public class SpawnMobAction extends Action {

    public SpawnMobAction() {
        id = "spawn";
    }

    @Override
    public void execute(ActionContext context, ConfigurationSection config) {
        String type = config.getString("type");
        String entity = config.getString("entity");
        Location location = context.getLocation();

        if (type == null || entity == null || location == null) return;

        Location centeredLocation = location.clone().add(0.5, 0, 0.5);

        if (type.equalsIgnoreCase("mythicmob")) {
            int level = config.getInt("level", 1);
            Optional<MythicMob> maybeMob = MythicBukkit.inst().getMobManager().getMythicMob(entity);

            if (maybeMob.isPresent()) {
                maybeMob.get().spawn(BukkitAdapter.adapt(centeredLocation), level);
            } else {
                NeomUtilities.getInstance().getLogger().warning("[HatchDebug] MythicMob '" + entity + "' wurde nicht gefunden!");
            }
        }

        if (type.equalsIgnoreCase("vanilla")) {
            World world = centeredLocation.getWorld();
            try {
                world.spawnEntity(centeredLocation, EntityType.valueOf(entity.toUpperCase()));
            } catch (IllegalArgumentException e) {
                NeomUtilities.getInstance().getLogger().warning("[HatchDebug] Vanilla EntityType '" + entity + "' ist ungültig!");
            }
        }
    }
}
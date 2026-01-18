package com.crayonsmp.neomUtilities.actions;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import org.bukkit.configuration.ConfigurationSection;

public class SoundAction extends Action {

    public SoundAction() {
        id = "sound";
    }

    @Override
    public void execute(ActionContext context, ConfigurationSection config) {
        String key = config.getString("key");
        float volume = (float) config.getDouble("volume", 1);
        float pitch = (float) config.getDouble("pitch", 1);

        if (key == null) {
            NeomUtilities.getInstance().getLogger().warning("Tried to play sound, but sound key was null!");
            return;
        }

        if (context.getLocation() != null){
            context.getLocation().getWorld().playSound(context.getLocation(), key, volume, pitch);
            return;
        }

        if (context.getPlayer() != null){
            context.getPlayer().playSound(context.getPlayer().getLocation(), key, volume, pitch);
            return;
        }

        NeomUtilities.getInstance().getLogger().warning("Tried to play sound, but location or player was null!");
    }
}

package com.crayonsmp.neomUtilities.conditions;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.model.Condition;
import com.crayonsmp.neomUtilities.model.ConditionContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class hasPermissionCondition extends Condition {
    public hasPermissionCondition(String id) {
        super(id);
    }

    @Override
    public boolean test(ConditionContext context, ConfigurationSection config) {
        String permission = config.getString("permission");
        Player player = context.getPlayer();

        if (permission == null) {
            NeomUtilities.getInstance().getLogger().warning("Permission has not been defined!");
            return false;
        }

        if (player == null) {
            NeomUtilities.getInstance().getLogger().warning("Player not found in the Context!");
            return false;
        }

        if (player.hasPermission(permission)) {
            return true;
        }

        return false;
    }
}

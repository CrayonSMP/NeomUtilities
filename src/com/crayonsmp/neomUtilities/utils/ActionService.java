package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.actions.*;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionService {

    private final Map<String, Action> registeredActions = new HashMap<>();

    public ActionService() {
        registerAction(new ChangeBlockAction());
        registerAction(new SoundAction());
        registerAction(new RemoveBlockAction());
        registerAction(new SpawnMobAction());
        registerAction(new SpawnParticleAction());
    }

    public void registerAction(Action action) {
        registeredActions.put(action.id.toLowerCase(), action);
    }

    public void executeAllActions(ConfigurationSection config, ActionContext context) {
        if (config == null || !config.contains("actions")) return;

        List<?> actionList = config.getList("actions");

        for (Object entry : actionList) {
            if (!(entry instanceof Map<?, ?> actionMap)) continue;

            String type = (String) actionMap.get("type");
            if (type == null) continue;

            Action action = registeredActions.get(type.toLowerCase());
            if (action == null) continue;

            Object valueData = actionMap.get("value");

            if (valueData instanceof Map<?, ?> valueMap) {
                MemoryConfiguration tempConfig = new MemoryConfiguration();

                for (Map.Entry<?, ?> entryMap : valueMap.entrySet()) {
                    tempConfig.set(entryMap.getKey().toString(), entryMap.getValue());
                }

                action.execute(context, tempConfig);
            }
        }
    }
}
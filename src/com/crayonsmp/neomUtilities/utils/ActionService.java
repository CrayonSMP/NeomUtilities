package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.actions.*;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import com.crayonsmp.neomUtilities.model.ConditionContext;
import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionService {

    private final Map<String, Action> registeredActions = new HashMap<>();
    private final ContextService contextService = NeomUtilities.getContextService();

    public ActionService() {
        registerAction(new ChangeBlockAction("change_block"));
        registerAction(new SoundAction("sound"));
        registerAction(new RemoveBlockAction("remove_block"));
        registerAction(new SpawnMobAction("spawn"));
        registerAction(new SpawnParticleAction("particle"));
    }

    public void registerAction(Action action) {
        registeredActions.put(action.getId().toLowerCase(), action);
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

            if (actionMap.containsKey("probability")) {
                Object probObj = actionMap.get("probability");

                if (probObj instanceof Number number) {
                    double probability = number.doubleValue();

                    if (probability > 1.0) {
                        probability = 1.0;
                    }

                    if (Math.random() > probability) {
                        continue;
                    }
                }
            }

            if (actionMap.containsKey("conditions")) {
                MemoryConfiguration actionSection = new MemoryConfiguration();
                actionMap.forEach((k, v) -> actionSection.set(k.toString(), v));

                ConditionContext conditionContext = new ConditionContext();
                conditionContext.setLocation(context.getLocation());
                conditionContext.setWorld(context.getWorld());
                conditionContext.setBlock(context.getBlock());
                conditionContext.setEntity(context.getEntity());
                conditionContext.setPlayer(context.getPlayer());

                if (!contextService.checkAllConditions(actionSection, conditionContext)) {
                    continue;
                }
            }

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
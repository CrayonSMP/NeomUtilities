package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.actions.*;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import com.crayonsmp.neomUtilities.model.ConditionContext;
import com.crayonsmp.neomUtilities.model.VariableContext;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionService {

    private final Map<String, Action> registeredActions = new HashMap<>();
    private final ConditionService conditionService;
    private final VariableService variableService;

    public ActionService(ConditionService conditionService, VariableService variableService) {
        this.conditionService = conditionService;
        this.variableService = variableService;
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
            if (!(entry instanceof Map<?, ?> rawMap)) continue;

            Map<String, Object> actionMap = (Map<String, Object>) replacePlaceholders(rawMap, context.getPlayer(), context);

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

                if (!conditionService.checkAllConditions(actionSection, conditionContext)) {
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

    private Object replacePlaceholders(Object source, Player player, ActionContext context) {
        if (source instanceof String str) {
            String replaced = resolveCustomVariables(str, player, context);

            if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                replaced = PlaceholderAPI.setPlaceholders(player, replaced);
            }
            return replaced;
        }

        if (source instanceof Map<?, ?> map) {
            Map<String, Object> newMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                newMap.put(entry.getKey().toString(), replacePlaceholders(entry.getValue(), player, context));
            }
            return newMap;
        }

        if (source instanceof List<?> list) {
            return list.stream().map(item -> replacePlaceholders(item, player, context)).toList();
        }

        return source;
    }

    private String resolveCustomVariables(String text, Player player, ActionContext actionContext) {
        if (!text.contains("$")) return text;

        VariableContext varContext = new VariableContext();
        varContext.setPlayer(player);
        varContext.setWorld(actionContext.getWorld());
        varContext.setLocation(actionContext.getLocation());
        varContext.setBlock(actionContext.getBlock());
        varContext.setEntity(actionContext.getEntity());

        StringBuilder sb = new StringBuilder(text);
        int currentIndex = 0;

        while ((currentIndex = sb.indexOf("$", currentIndex)) != -1) {
            int end = sb.indexOf("$", currentIndex + 1);
            if (end == -1) break;

            String varName = sb.substring(currentIndex + 1, end);
            String value = variableService.getVariableValue(varName, varContext);

            if (value != null) {
                sb.replace(currentIndex, end + 1, value);
                currentIndex += value.length();
            } else {
                currentIndex = end + 1;
            }
        }
        return sb.toString();
    }
}
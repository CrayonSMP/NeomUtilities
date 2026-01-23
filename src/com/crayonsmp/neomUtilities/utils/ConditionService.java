package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.conditions.hasPermissionCondition;
import com.crayonsmp.neomUtilities.conditions.isBlockCondition;
import com.crayonsmp.neomUtilities.model.ActionContext;
import com.crayonsmp.neomUtilities.model.Condition;
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

public class ConditionService {
    private final Map<String, Condition> registeredConditions = new HashMap<>();
    private final VariableService variableService;

    public ConditionService(VariableService variableService) {
        this.variableService = variableService;
        registerCondition(new hasPermissionCondition("has-permission"));
        registerCondition(new isBlockCondition("is-block"));
    }

    public void registerCondition(Condition condition) {
        registeredConditions.put(condition.getId().toLowerCase(), condition);
    }

    public boolean checkAllConditions(ConfigurationSection config, ConditionContext context) {
        if (config == null || !config.contains("conditions")) return true;

        List<?> conditionList = config.getList("conditions");

        boolean allConditions = true;
        
        assert conditionList != null;
        for (Object entry : conditionList) {
            if (!(entry instanceof Map<?, ?> rawMap)) continue;

            Map<String, Object> conditionMap = (Map<String, Object>) replacePlaceholders(rawMap, context.getPlayer(), context);

            String type = (String) conditionMap.get("type");
            if (type == null) continue;

            Condition condition = registeredConditions.get(type.toLowerCase());
            if (condition == null) continue;
            
            boolean inverted = (Boolean) conditionMap.get("inverted");
            condition.setInverted(inverted);

            Object valueData = conditionMap.get("terms");

            if (valueData instanceof Map<?, ?> valueMap) {
                MemoryConfiguration tempConfig = new MemoryConfiguration();

                for (Map.Entry<?, ?> entryMap : valueMap.entrySet()) {
                    tempConfig.set(entryMap.getKey().toString(), entryMap.getValue());
                }

                if (!condition.test(context, tempConfig)) {
                    if (inverted) {
                        continue;
                    }
                    allConditions = false;
                }
            }
        }
        
        return allConditions;
    }

    private Object replacePlaceholders(Object source, Player player, ConditionContext context) {
        if (source instanceof String str) {
            str = resolveCustomVariables(str, player, context);

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                str = PlaceholderAPI.setPlaceholders(player, str);
            }
            return str;
        } else if (source instanceof Map<?, ?> map) {
            Map<String, Object> newMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                newMap.put(entry.getKey().toString(), replacePlaceholders(entry.getValue(), player, context));
            }
            return newMap;
        } else if (source instanceof List<?> list) {
            return list.stream().map(item -> replacePlaceholders(item, player, context)).toList();
        }
        return source;
    }

    private String resolveCustomVariables(String text, Player player, ConditionContext conditionContext) {
        if (!text.contains("$")) return text;

        VariableContext varContext = new VariableContext();
        varContext.setPlayer(player);
        varContext.setWorld(conditionContext.getWorld());
        varContext.setLocation(conditionContext.getLocation());
        varContext.setBlock(conditionContext.getBlock());
        varContext.setEntity(conditionContext.getEntity());

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

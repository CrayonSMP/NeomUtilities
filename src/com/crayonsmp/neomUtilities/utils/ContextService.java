package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.actions.*;
import com.crayonsmp.neomUtilities.conditions.hasPermission;
import com.crayonsmp.neomUtilities.model.Action;
import com.crayonsmp.neomUtilities.model.ActionContext;
import com.crayonsmp.neomUtilities.model.Condition;
import com.crayonsmp.neomUtilities.model.ConditionContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextService {
    private final Map<String, Condition> registeredConditions = new HashMap<>();

    public ContextService() {
        registerCondition(new hasPermission("has-permission"));
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
            if (!(entry instanceof Map<?, ?> conditionMap)) continue;

            String type = (String) conditionMap.get("type");
            if (type == null) continue;

            Condition condition = registeredConditions.get(type.toLowerCase());
            if (condition == null) continue;
            
            boolean inverted = (Boolean) conditionMap.get("inverted");

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
}

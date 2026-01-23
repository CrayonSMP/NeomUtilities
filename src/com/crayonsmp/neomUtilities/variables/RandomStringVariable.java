package com.crayonsmp.neomUtilities.variables;

import com.crayonsmp.neomUtilities.model.Variable;
import com.crayonsmp.neomUtilities.model.VariableContext;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class RandomStringVariable extends Variable {
    public RandomStringVariable(String id) { super(id); }

    @Override
    public Object resolve(VariableContext context, ConfigurationSection config) {
        ConfigurationSection options = config.getConfigurationSection("options");
        if (options == null) return "";

        double totalWeight = 0;
        Map<String, Double> weights = new HashMap<>();

        for (String key : options.getKeys(false)) {
            double prob = options.getDouble(key + ".probability", 0.0);
            weights.put(key, prob);
            totalWeight += prob;
        }

        double random = Math.random() * totalWeight;
        double current = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            current += entry.getValue();
            if (random <= current) return entry.getKey();
        }
        return "";
    }
}
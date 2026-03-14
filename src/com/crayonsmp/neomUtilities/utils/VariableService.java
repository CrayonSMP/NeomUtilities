package com.crayonsmp.neomUtilities.utils;

import com.crayonsmp.neomUtilities.model.Variable;
import com.crayonsmp.neomUtilities.model.VariableContext;
import com.crayonsmp.neomUtilities.variables.RandomStringVariable;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;

public class VariableService {
    private final Map<String, Variable> registeredTypes = new HashMap<>();
    private final Map<String, ConfigurationSection> variableConfigs = new HashMap<>();

    public VariableService() {
        registerType(new RandomStringVariable("string"));
    }

    public void registerType(Variable var) {
        registeredTypes.put(var.getId().toLowerCase(), var);
    }

    public void loadVariables(ConfigurationSection config) {
        variableConfigs.clear();
        if (config == null || !config.contains("variables")) return;

        ConfigurationSection varSection = config.getConfigurationSection("variables");
        for (String varName : varSection.getKeys(false)) {
            variableConfigs.put(varName, varSection.getConfigurationSection(varName));
        }
    }
    public Object getVariableRawValue(String varName, VariableContext context) {
        ConfigurationSection settings = variableConfigs.get(varName);
        if (settings == null) return null;

        String type = settings.getString("type");
        Variable varType = registeredTypes.get(type != null ? type.toLowerCase() : "");

        if (varType != null) {
            return varType.resolve(context, settings);
        }
        return null;
    }

    public String getVariableValue(String varName, VariableContext context) {
        ConfigurationSection settings = variableConfigs.get(varName);
        if (settings == null) return null;

        String type = settings.getString("type");
        Variable varType = registeredTypes.get(type != null ? type.toLowerCase() : "");

        if (varType != null) {
            Object result = varType.resolve(context, settings);
            return result != null ? result.toString() : "";
        }
        return "";
    }
}
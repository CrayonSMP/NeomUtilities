package com.crayonsmp.neomUtilities.model;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Variable {
    private final String id;

    public Variable(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract Object resolve(VariableContext context, ConfigurationSection config);
}
package com.crayonsmp.neomUtilities.model;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Action {
    private final String id;

    public Action(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract void execute(ActionContext context, ConfigurationSection config);
}
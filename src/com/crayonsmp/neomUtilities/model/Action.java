package com.crayonsmp.neomUtilities.model;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Action {
    public String id;
    public abstract void execute(ActionContext context, ConfigurationSection config);
}

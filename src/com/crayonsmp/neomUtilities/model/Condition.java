package com.crayonsmp.neomUtilities.model;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Condition {
    private final String id;
    private boolean inverted = false;

    public Condition(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract boolean test(ConditionContext context, ConfigurationSection config);

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
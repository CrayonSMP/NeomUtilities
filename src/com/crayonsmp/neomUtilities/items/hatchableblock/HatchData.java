package com.crayonsmp.neomUtilities.items.hatchableblock;

import org.bukkit.Location;

public class HatchData {
    public String type;
    public long ticks;
    public Location location;

    public HatchData(Location location, String type, long ticks) {
        this.location = location;
        this.type = type;
        this.ticks = ticks;
    }
}
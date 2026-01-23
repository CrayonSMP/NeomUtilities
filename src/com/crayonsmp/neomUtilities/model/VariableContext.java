package com.crayonsmp.neomUtilities.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class VariableContext {
    private Player player;
    private World world;
    private Location location;
    private Block block;
    private Entity entity;

    //Getters
    public Player getPlayer() {
        return player;
    }
    public World getWorld() {
        return world;
    }
    public Location getLocation() {
        return location;
    }
    public Block getBlock() {
        return block;
    }
    public Entity getEntity() {
        return entity;
    }

    //Setters
    public void setBlock(Block block) {
        this.block = block;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public void setWorld(World world) {
        this.world = world;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}

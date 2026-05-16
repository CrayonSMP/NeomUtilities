package com.crayonsmp.neomUtilities.items.ender_pouch;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class EnderPouchService {
    private Key enderPouchKey;
    private String sound;
    private float volume;
    private float pitch;

    public EnderPouchService(Plugin plugin) {
        FileConfiguration config = NeomUtilities.getInstance().getConfig();

        String id = config.getString("ender-pouch.item-id", "tamashii:ender-pouch");
        if (CraftEngineItems.byId(Key.from(id)) == null) {
            NeomUtilities.getInstance().getLogger().warning("Ender Pouch item ID '" + id + "' is invalid! Feature disabled.");
            return;
        }

        this.enderPouchKey = Key.from(id);

        this.sound = config.getString("ender-pouch.open-sound", "minecraft:item.ender_pearl.throw");
        this.volume = (float) config.getDouble("ender-pouch.open-volume", 1.0);
        this.pitch = (float) config.getDouble("ender-pouch.open-pitch", 1.0);

        plugin.getServer().getPluginManager().registerEvents(new EnderPouchListener(this), plugin);
    }

    // Getters so the Listener can access the data
    public Key getEnderPouchKey() { return enderPouchKey; }
    public String getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }
}
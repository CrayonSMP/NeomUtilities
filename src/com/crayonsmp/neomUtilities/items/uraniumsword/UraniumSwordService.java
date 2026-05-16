package com.crayonsmp.neomUtilities.items.uraniumsword;

import com.crayonsmp.neomUtilities.NeomUtilities;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class UraniumSwordService {
    private Key uranimActivatedSwordKey;
    private Key uranimDeactivatedSwordKey;
    private String soundActivate;
    private float volumeActivate;
    private float pitchActivate;
    private String soundDeactivate;
    private float volumeDeactivate;
    private float pitchDeactivate;
    private int deactivateTimeTicks;
    private int activateKillCount;

    public UraniumSwordService(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();

        String idActivated = config.getString("uranimsword.item-id-activated", "neom:uranium_sword_activated");
        String idDeactivated = config.getString("uranimsword.item-id-deactivated", "neom:uranium_sword_deactivated");

        if (CraftEngineItems.byId(Key.from(idActivated)) == null || CraftEngineItems.byId(Key.from(idDeactivated)) == null) {
            plugin.getLogger().warning("Uranium Sword item IDs ('" + idActivated + "' / '" + idDeactivated + "') are invalid! Feature disabled.");
            return;
        }

        this.uranimActivatedSwordKey = Key.from(idActivated);
        this.uranimDeactivatedSwordKey = Key.from(idDeactivated);

        this.soundActivate = config.getString("uranimsword.activatesound.sound", "minecraft:item.ender_pearl.throw");
        this.volumeActivate = (float) config.getDouble("uranimsword.activatesound.volume", 1.0);
        this.pitchActivate = (float) config.getDouble("uranimsword.activatesound.pitch", 1.0);

        this.soundDeactivate = config.getString("uranimsword.deactivatesound.sound", "minecraft:item.ender_pearl.throw");
        this.volumeDeactivate = (float) config.getDouble("uranimsword.deactivatesound.volume", 1.0);
        this.pitchDeactivate = (float) config.getDouble("uranimsword.deactivatesound.pitch", 1.0);

        this.deactivateTimeTicks = config.getInt("uranimsword.deactivate-ticks", 200);
        this.activateKillCount = config.getInt("uranimsword.activate-kill-count", 5);

        plugin.getServer().getPluginManager().registerEvents(new UraniumSwordListener(plugin, this), plugin);
    }

    public String getSoundDeactivate() {
        return soundDeactivate;
    }

    public Key getUranimActivatedSwordKey() {
        return uranimActivatedSwordKey;
    }

    public Key getUranimDeactivatedSwordKey() {
        return uranimDeactivatedSwordKey;
    }

    public void setUranimDeactivatedSwordKey(Key uranimDeactivatedSwordKey) {
        this.uranimDeactivatedSwordKey = uranimDeactivatedSwordKey;
    }

    public String getSoundActivate() {
        return soundActivate;
    }

    public float getVolumeActivate() {
        return volumeActivate;
    }

    public float getPitchActivate() {
        return pitchActivate;
    }

    public float getVolumeDeactivate() {
        return volumeDeactivate;
    }

    public float getPitchDeactivate() {
        return pitchDeactivate;
    }

    public int getDeactivateTimeTicks() {
        return deactivateTimeTicks;
    }

    public int getActivateKillCount() {
        return activateKillCount;
    }
}

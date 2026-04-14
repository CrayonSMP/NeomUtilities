package com.crayonsmp.neomUtilities.effects.radiation;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RadiationService {
    NamespacedKey radiationKey = new NamespacedKey("neom", "radiation");

    public void init() {

    }

    public void addRadiation(Player player, int amount) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (!container.has(radiationKey, PersistentDataType.INTEGER)) {
            container.set(radiationKey, PersistentDataType.INTEGER, amount);
            return;
        }

        int currentRadiation = container.get(radiationKey, PersistentDataType.INTEGER);
        container.remove(radiationKey);
        container.set(radiationKey, PersistentDataType.INTEGER, currentRadiation + amount);
    }

    public void removeRadiation(Player player, int amount) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (!container.has(radiationKey, PersistentDataType.INTEGER)) {
            return;
        }

        int currentRadiation = container.get(radiationKey, PersistentDataType.INTEGER);
        container.remove(radiationKey);
        container.set(radiationKey, PersistentDataType.INTEGER, currentRadiation - amount);
    }


}

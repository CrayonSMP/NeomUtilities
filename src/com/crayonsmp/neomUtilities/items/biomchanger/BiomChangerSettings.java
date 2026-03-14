package com.crayonsmp.neomUtilities.items.biomchanger;

import java.util.List;

public class BiomChangerSettings {
    String biomeId;
    int radius;
    boolean removeItems;
    List<String> allowedPlayers;

    BiomChangerSettings(String biomeId, int radius, boolean removeItems, List<String> allowedPlayers) {
        this.biomeId = biomeId;
        this.radius = radius;
        this.removeItems = removeItems;
        this.allowedPlayers = allowedPlayers;
    }
}
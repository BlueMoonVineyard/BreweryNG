package com.dre.brewery.filedata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

public class CraftedBrewTracker {
    public static Map<String, Set<String>> playerData = new HashMap<>();

    public static void loadFrom(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            playerData.put(key, new HashSet<>(section.getStringList(key)));
        }
    }
    public static void saveTo(ConfigurationSection section) {
        for (String key : playerData.keySet()) {
            section.set(key, playerData.get(key).stream().toList());
        }
    }
    public static void playerHasMadeBrew(String uuid, String brewID) {
        if (!playerData.containsKey(uuid)) {
            playerData.put(uuid, new HashSet<>());
        }
        playerData.get(uuid).add(brewID);
    }
    public static Set<String> playerBrews(String uuid) {
        if (!playerData.containsKey(uuid)) {
            playerData.put(uuid, new HashSet<>());
        }
        return playerData.get(uuid);
    }
}

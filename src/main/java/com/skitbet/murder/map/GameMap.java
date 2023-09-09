package com.skitbet.murder.map;

import org.bukkit.Location;
import org.bukkit.World;

public interface GameMap {
    boolean load();
    void unload();
    boolean restoreFromSource();

    boolean isLoaded();
    World getWorld();
    String getName();
    Location getRandomSpawn();

}

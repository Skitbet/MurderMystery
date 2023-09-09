package com.skitbet.murder.map;

import com.google.common.base.Strings;
import com.skitbet.murder.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LocalGameMap implements GameMap {

    private final File sourceWorldFolder;
    private File activeWorldFolder;
    private List<String> spawnStrings;

    private World bukkitWorld;
    private String name;

    public LocalGameMap(File worldFolder, String worldName, List<String> locationStrings, boolean loadOnInit) {
        this.sourceWorldFolder = new File(
                worldFolder,
                worldName
        );

        this.spawnStrings = locationStrings;
        this.name = worldName;

        if (loadOnInit) load();
    }

    @Override
    public boolean load() {
        if (isLoaded()) return true;

        this.activeWorldFolder = new File(
                Bukkit.getWorldContainer().getParentFile(),
                sourceWorldFolder.getName() + "_active_" + System.currentTimeMillis()
        );

        try {
            FileUtil.copy(sourceWorldFolder, activeWorldFolder);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load map " + sourceWorldFolder.getName());
            e.printStackTrace();
            return false;
        }

        this.bukkitWorld = Bukkit.createWorld(
                new WorldCreator(activeWorldFolder.getName())
        );

        if (bukkitWorld != null) this.bukkitWorld.setAutoSave(false);
        return isLoaded();
    }

    @Override
    public void unload() {
        if (bukkitWorld != null) Bukkit.unloadWorld(bukkitWorld, false);
        if (activeWorldFolder != null) FileUtil.delete(activeWorldFolder);

        bukkitWorld = null;
        activeWorldFolder = null;
    }

    @Override
    public boolean restoreFromSource() {
        unload();
        return load();
    }

    @Override
    public boolean isLoaded() {
        return getWorld() != null;
    }

    @Override
    public World getWorld() {
        if (activeWorldFolder == null) {
            return null;
        }
        return Bukkit.getWorld(activeWorldFolder.getName());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Location getRandomSpawn() {
        String randomString = spawnStrings.stream().skip(new Random().nextInt(spawnStrings.size())).findFirst().orElse(null);
        return locationFromString(randomString);
    }
    private Location locationFromString(String string) {
        String[] values = string.split(":");
        return new Location(getWorld(), Double.valueOf(values[0]), Double.valueOf(values[1]), Double.valueOf(values[2]), Float.valueOf(values[3]), Float.valueOf(values[4]));
    }
}

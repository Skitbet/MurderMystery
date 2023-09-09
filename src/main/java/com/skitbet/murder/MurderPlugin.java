package com.skitbet.murder;

import com.skitbet.murder.commands.AddSpawnCommand;
import com.skitbet.murder.game.GameManager;
import com.skitbet.murder.listener.NametagDisabler;
import com.skitbet.murder.listener.SaturationListener;
import com.skitbet.murder.map.LocalGameMap;
import io.github.thatkawaiisam.assemble.Assemble;
import io.github.thatkawaiisam.assemble.AssembleStyle;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MurderPlugin extends JavaPlugin {

    public static MurderPlugin INSTANCE;

    @Getter private GameManager gameManager;

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.gameManager = new GameManager();
        saveDefaultConfig();

        getDataFolder().mkdirs();

        File gameMapsFolder = new File(getDataFolder(), "maps");
        if (!gameMapsFolder.exists()) {
            gameMapsFolder.mkdirs();
        }

        getServer().getPluginManager().registerEvents(new NametagDisabler(), this);
        getServer().getPluginManager().registerEvents(new SaturationListener(), this);

        getCommand("addspawn").setExecutor(new AddSpawnCommand());

        Assemble assemble = new Assemble(this, new ScoreboardAdapter());
        assemble.setTicks(1);
        assemble.setAssembleStyle(AssembleStyle.MODERN);
    }

    @Override
    public void onDisable() {
        if (this.gameManager.getCurrentMap() != null && this.gameManager.getCurrentMap().isLoaded()) {
            this.gameManager.getCurrentMap().unload();
        }
    }

    public LocalGameMap getRandomLocalMap() {
        ConfigurationSection mapsSection = getConfig().getConfigurationSection("maps");
        if (mapsSection != null) {
            Set<String> mapNames = mapsSection.getKeys(false);
            String randomMap = mapNames.stream().skip(new Random().nextInt(mapNames.size())).findFirst().orElse(null);
            if (randomMap != null) {
                List<String> spawns = mapsSection.getStringList(randomMap + ".spawns");
                return new LocalGameMap(new File(getDataFolder(), "maps"), randomMap, spawns, false);
            }
        }

//        return new LocalGameMap(new File(getDataFolder(), "maps"), selected, false);
        return null;
    }
}
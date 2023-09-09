package com.skitbet.murder.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class SaturationListener implements Listener {

    @EventHandler
    public void onSaturationUpdate(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

}
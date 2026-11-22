package me.yozupi.listener.impl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class AntiPortalCreate implements Listener {

    @EventHandler
    private void onPortalCreate(PortalCreateEvent event) {
        event.setCancelled(true);
    }

}

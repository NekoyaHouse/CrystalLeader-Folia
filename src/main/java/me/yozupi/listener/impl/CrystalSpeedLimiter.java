package me.yozupi.listener.impl;

import me.yozupi.CrystalLeader;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CrystalSpeedLimiter implements Listener {
    @EventHandler
    private void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof EnderCrystal
                && event.getDamager() instanceof Player
                && event.getEntity().getTicksLived() <= CrystalLeader.configInt("min_crystal_age")) {
            event.setCancelled(true);
        }
    }
}

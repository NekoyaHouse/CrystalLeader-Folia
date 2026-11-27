package me.yozupi.listener.impl;

import me.yozupi.CrystalLeader;
import me.yozupi.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnRegular implements Listener {
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!CrystalLeader.INSTANCE.getConfig().getBoolean("RespawnRegular.enable")) return;

        event.setRespawnLocation(new Location(Utils.OVERWORLD, 0.5, 10, 0.5, -90.0f, 0.0f));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!CrystalLeader.INSTANCE.getConfig().getBoolean("RespawnRegular.enable")) return;

        event.getPlayer().teleportAsync(new Location(Utils.OVERWORLD, 0.5, 10, 0.5, -90.0f, 0.0f)).exceptionally(ex -> {
            event.getPlayer().sendMessage("§c传送至重生点失败: " + ex.getMessage());
            return null;
        });
    }
}

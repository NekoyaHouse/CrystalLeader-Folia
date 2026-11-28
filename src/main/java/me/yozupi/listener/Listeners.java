package me.yozupi.listener;

import me.yozupi.CrystalLeader;
import me.yozupi.listener.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Listeners {
    public static void register() {
        add(new AntiPhase());
        add(new AntiPortalCreate());
        add(new CrystalSpeedLimiter());
        add(new FrameItems());
        add(new Player());
        add(new RespawnRegular());
    }

    private static void add(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, CrystalLeader.INSTANCE);
    }
}

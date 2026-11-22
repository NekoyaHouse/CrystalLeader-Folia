package me.yozupi.listener;

import me.yozupi.CrystalLeader;
import me.yozupi.listener.impl.FrameItems;
import me.yozupi.listener.impl.Player;
import me.yozupi.listener.impl.RespawnRegular;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Listeners {
    public static void register() {
        register(new FrameItems());
        register(new Player());
        register(new RespawnRegular());
    }

    private static void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, CrystalLeader.INSTANCE);
    }
}
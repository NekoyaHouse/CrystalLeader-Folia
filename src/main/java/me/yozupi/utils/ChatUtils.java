package me.yozupi.utils;

import me.yozupi.CrystalLeader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatUtils {
    public static String cantUseThisInPvp() {
        return message("cantUseThisInPvp", "&e这里是 PVP 场地，暂时不能用这个命令喵～先回安全区吧！");
    }

    public static String message(String key, String fallback) {
        return MessageUtils.withColor(CrystalLeader.INSTANCE.getConfig().getString("Messages." + key, fallback));
    }

    public static String message(String key, String fallback, int seconds) {
        return message(key, fallback).replace("{seconds}", Integer.toString(seconds));
    }

    public static String getPrefix() {
        String prefix = CrystalLeader.configString("prefix");
        return prefix == null ? "" : MessageUtils.withColor(prefix);
    }

    public static void sendMessage(String string) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(string);
        }
    }

    public static void sendMessage(String... string) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(string);
        }
    }
}

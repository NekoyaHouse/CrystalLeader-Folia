package me.yozupi.utils;

import me.yozupi.CrystalLeader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatUtils {
    // General
    public static String cantUseThisInPvp() {
        return "你不能在PVP场地使用这个命令";
    }

    // Prefixes
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

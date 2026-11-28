package me.yozupi;

import me.yozupi.command.Commands;
import me.yozupi.listener.Listeners;
import me.yozupi.tasks.ArenaClearer;
import me.yozupi.tasks.DroppedClearer;
import me.yozupi.utils.MessageUtils;
import me.yozupi.utils.TimerUtils;
import me.yozupi.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CrystalLeader extends JavaPlugin {
    public static CrystalLeader INSTANCE;

    private ArenaClearer arenaClearer;
    private DroppedClearer droppedClearer;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Long time = calcTime(() -> {
            getLogger().info(MessageUtils.withColor("&aStart loading CrystalLeader..."));

            saveDefaultConfig();
            getLogger().info(MessageUtils.withColor("&aLoaded configuration"));

            // 这里注册指令
            Commands.register();

            // Utils的初始化
            Utils.onEnable(this);

            // 这里注册事件
            Listeners.register();

            arenaClearer = new ArenaClearer(this);
            arenaClearer.start();

            droppedClearer = new DroppedClearer(this);
            droppedClearer.start();

            getLogger().info(ChatColor.BLUE + """
                    
                       ____                _        _   _                   _          \s
                      / ___|_ __ _   _ ___| |_ __ _| | | |    ___  __ _  __| | ___ _ __\s
                     | |   | '__| | | / __| __/ _` | | | |   / _ \\/ _` |/ _` |/ _ \\ '__|
                     | |___| |  | |_| \\__ \\ || (_| | | | |__|  __/ (_| | (_| |  __/ |  \s
                      \\____|_|   \\__, |___/\\__\\__,_|_| |_____\\___|\\__,_|\\__,_|\\___|_|  \s
                                 |___/                                                 \s
                    """);
        });

        getLogger().info(MessageUtils.withColor("Loaded CrystalLeader, it took time: " + time + "ms"));
    }

    @Override
    public void onDisable() {
        if (arenaClearer != null) arenaClearer.stop();
        if (droppedClearer != null) droppedClearer.stop();
        Utils.onDisable();
        getLogger().info(MessageUtils.withColor(ChatColor.RED + "Disabled CrystalLeader, thanks for using OwO"));
    }

    public ArenaClearer getArenaClearer() {
        return arenaClearer;
    }

    private Long calcTime(Runnable runnable) {
        TimerUtils timer = new TimerUtils();
        runnable.run();
        return timer.passedMs();
    }

    public static int configInt(String path) {
        return INSTANCE.getConfig().getInt(path);
    }

    public static double configDouble(String path) {
        return INSTANCE.getConfig().getDouble(path);
    }

    public static String configString(String path) {
        return INSTANCE.getConfig().getString(path);
    }

    public static List<String> configStringList(String path) {
        return INSTANCE.getConfig().getStringList(path);
    }
}

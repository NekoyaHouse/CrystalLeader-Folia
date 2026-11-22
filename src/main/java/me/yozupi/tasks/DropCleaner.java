package me.yozupi.tasks;

import me.yozupi.CrystalLeader;
import me.yozupi.utils.ChatUtils;
import me.yozupi.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

public class DropCleaner {
    private final JavaPlugin plugin;
    private final int intervalMinutes;
    private final String message;

    public DropCleaner(JavaPlugin plugin) {
        this.plugin = plugin;
        this.intervalMinutes = Math.max(1, CrystalLeader.INSTANCE.getConfig().getInt("DropCleaner.interval-minutes", 2));
        this.message = MessageUtils.withColor(CrystalLeader.INSTANCE.getConfig().getString("DropCleaner.message", "&e全服掉落物已清理！"));
    }

    public void start() {
        if (!CrystalLeader.INSTANCE.getConfig().getBoolean("DropCleaner.enable", true)) {
            CrystalLeader.INSTANCE.getLogger().info("DropCleaner is disabled");
            return;
        }

        if (CrystalLeader.INSTANCE.getConfig().getInt("DropCleaner.interval-minutes", 2) <= 0) {
            CrystalLeader.INSTANCE.getLogger().warning("DropCleaner.interval-minutes must be positive, using default 2 minutes");
        }

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> cleanDrops(),
                0L,
                intervalMinutes * 20L * 60L
        );
    }

    private void cleanDrops() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                    removed++;
                }
            }
        }

        if (removed > 0) {
            ChatUtils.sendMessage(ChatUtils.getPrefix() + message);
        }
    }
}

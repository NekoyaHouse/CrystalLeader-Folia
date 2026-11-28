package me.yozupi.tasks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.yozupi.CrystalLeader;
import me.yozupi.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DroppedClearer implements Listener {
    private final CrystalLeader plugin;
    private final ConcurrentHashMap<UUID, Item> items = new ConcurrentHashMap<>();
    private Set<String> worlds = Set.of();
    private volatile boolean stopped;
    private ScheduledTask timer;

    public DroppedClearer(CrystalLeader plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("DroppedItemClear.enable", true)) return;
        long interval = plugin.getConfig().getLong("DroppedItemClear.interval-seconds", 120);
        if (interval <= 0) {
            plugin.getLogger().warning("DroppedItemClear.interval-seconds 必须大于 0，已使用默认值 120 秒。");
            interval = 120;
        }
        worlds = Set.copyOf(plugin.getConfig().getStringList("DroppedItemClear.worlds"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            if (stopped) return;
            for (World world : Bukkit.getWorlds()) {
                if (!includesWorld(world.getName())) continue;
                for (Chunk chunk : world.getLoadedChunks()) {
                    Bukkit.getRegionScheduler().run(plugin, world, chunk.getX(), chunk.getZ(), regionTask -> {
                        if (stopped || !chunk.isLoaded()) return;
                        for (Entity entity : chunk.getEntities()) track(entity);
                    });
                }
            }
        });
        timer = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> clear(), interval, interval, TimeUnit.SECONDS);
        plugin.getLogger().info("掉落物清理已启用：每 " + interval + " 秒清理一次。");
    }

    private boolean includesWorld(String name) {
        return worlds.isEmpty() || worlds.contains(name);
    }

    private void track(Entity entity) {
        if (!stopped && entity instanceof Item item && includesWorld(item.getWorld().getName())) {
            items.put(item.getUniqueId(), item);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        track(event.getEntity());
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) track(entity);
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Item) items.remove(entity.getUniqueId(), entity);
        }
    }

    private void clear() {
        if (stopped) return;
        broadcast();
        items.forEach((id, item) -> {
            if (stopped) return;
            ScheduledTask scheduled = item.getScheduler().run(plugin, task -> {
                if (stopped) return;
                if (item.isValid() && includesWorld(item.getWorld().getName())) item.remove();
                items.remove(id, item);
            }, () -> items.remove(id, item));
            if (scheduled == null) items.remove(id, item);
        });
    }

    private void broadcast() {
        String message = ChatUtils.getPrefix() + ChatUtils.message("droppedItemClear", "&e清洁小猫出动！正在收拾地上的掉落物喵～");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(plugin, task -> {
                if (!stopped) player.sendMessage(message);
            }, null);
        }
    }

    public void stop() {
        stopped = true;
        if (timer != null) timer.cancel();
        HandlerList.unregisterAll(this);
        items.clear();
    }
}

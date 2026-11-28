package me.yozupi.tasks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.yozupi.CrystalLeader;
import me.yozupi.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ArenaClearer {
    private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int BLOCKS_PER_TICK = 4096;
    private static final int CONCURRENT_CHUNKS = 4;

    private final CrystalLeader plugin;
    private final List<Arena> arenas = new ArrayList<>();
    private final AtomicBoolean clearing = new AtomicBoolean();
    private final Set<ScheduledTask> chunkTasks = ConcurrentHashMap.newKeySet();
    private volatile boolean stopped;
    private ScheduledTask clockTask;
    private ScheduledTask countdownTask;
    private ZonedDateTime nextClear;
    private boolean warnedThirty;
    private boolean warnedFive;

    public ArenaClearer(CrystalLeader plugin) {
        this.plugin = plugin;
    }

    public void start() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("ArenaClear");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            ConfigurationSection arena = section.getConfigurationSection(name);
            if (arena == null) continue;
            String worldName = arena.getString("world");
            World world = worldName == null ? null : Bukkit.getWorld(worldName);
            if (world == null || !hasCoordinates(arena)) {
                plugin.getLogger().warning("跳过清场区域 " + name + ": 世界不存在或坐标配置不完整。");
                continue;
            }
            int minY = Math.clamp(arena.getInt("from.y"), world.getMinHeight(), arena.getInt("to.y"));
            int maxY = Math.clamp(arena.getInt("from.y"), arena.getInt("to.y"), world.getMaxHeight() - 1);
            if (minY > maxY) {
                plugin.getLogger().warning("跳过清场区域 " + name + ": Y 坐标超出世界高度。");
                continue;
            }
            arenas.add(new Arena(world,
                    Math.min(arena.getInt("from.x"), arena.getInt("to.x")),
                    Math.max(arena.getInt("from.x"), arena.getInt("to.x")), minY, maxY,
                    Math.min(arena.getInt("from.z"), arena.getInt("to.z")),
                    Math.max(arena.getInt("from.z"), arena.getInt("to.z"))));
        }
        if (arenas.isEmpty()) return;
        nextClear = nextClearTime(ZonedDateTime.now(TIME_ZONE));
        clockTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> checkClock(), 1, 1, TimeUnit.SECONDS);
        plugin.getLogger().info("场地清理已启用：每天北京时间 12:00 和 00:00，保留基岩。");
    }

    private static boolean hasCoordinates(ConfigurationSection section) {
        for (String corner : List.of("from", "to")) {
            for (String axis : List.of("x", "y", "z")) {
                if (!section.isInt(corner + "." + axis)) return false;
            }
        }
        return true;
    }

    static ZonedDateTime nextClearTime(ZonedDateTime now) {
        ZonedDateTime noon = now.toLocalDate().atTime(12, 0).atZone(now.getZone());
        return now.isBefore(noon) ? noon : now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
    }

    private void checkClock() {
        if (stopped) return;
        ZonedDateTime now = ZonedDateTime.now(TIME_ZONE);
        long remaining = Duration.between(now, nextClear).toMillis();
        if (remaining <= 0) {
            nextClear = nextClearTime(now);
            warnedThirty = false;
            warnedFive = false;
            clear();
        } else if (remaining <= 5000 && !warnedFive) {
            warnedThirty = true;
            warnedFive = true;
            broadcast("&e场地将在 5 秒后清理！");
        } else if (remaining <= 30000 && !warnedThirty) {
            warnedThirty = true;
            broadcast("&e场地将在 30 秒后清理！");
        }
    }

    public enum ClearResult {
        STARTED,
        BUSY,
        UNAVAILABLE
    }

    public ClearResult clear() {
        return clear(0);
    }

    public synchronized ClearResult clear(int seconds) {
        if (seconds < 0 || seconds > 86400) throw new IllegalArgumentException("倒计时必须为 0 到 86400 秒");
        if (stopped || arenas.isEmpty()) return ClearResult.UNAVAILABLE;
        if (!clearing.compareAndSet(false, true)) return ClearResult.BUSY;
        if (seconds == 0) {
            beginClear();
        } else {
            try {
                broadcast("&e场地将在 " + seconds + " 秒后清理！");
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
                int[] lastAnnounced = {seconds};
                countdownTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
                    synchronized (ArenaClearer.this) {
                        if (stopped) {
                            task.cancel();
                            return;
                        }
                        long nanos = deadline - System.nanoTime();
                        int remaining = (int) Math.max(0, (nanos + 999_999_999L) / 1_000_000_000L);
                        if (remaining == 0) {
                            task.cancel();
                            countdownTask = null;
                            beginClear();
                        } else if (remaining != lastAnnounced[0] && (remaining == 30 || remaining <= 5)) {
                            lastAnnounced[0] = remaining;
                            broadcast("&e场地将在 " + remaining + " 秒后清理！");
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS);
            } catch (RuntimeException exception) {
                clearing.set(false);
                throw exception;
            }
        }
        return ClearResult.STARTED;
    }

    private void beginClear() {
        broadcast("&e正在清理场地，请注意脚下！");
        ConcurrentLinkedQueue<ChunkJob> jobs = new ConcurrentLinkedQueue<>();
        for (Arena arena : arenas) {
            for (int x = arena.minX >> 4; x <= arena.maxX >> 4; x++) {
                for (int z = arena.minZ >> 4; z <= arena.maxZ >> 4; z++) {
                    jobs.add(new ChunkJob(arena, x, z));
                }
            }
        }
        AtomicBoolean failed = new AtomicBoolean();
        CompletableFuture<?>[] workers = new CompletableFuture<?>[CONCURRENT_CHUNKS];
        for (int i = 0; i < workers.length; i++) workers[i] = clearNext(jobs, failed);
        CompletableFuture.allOf(workers).whenComplete((unused, error) -> {
            clearing.set(false);
            if (!stopped)
                broadcast(failed.get() || error != null ? "&c场地清理出现错误，请管理员查看控制台。" : "&a场地清理完成！");
        });

    }

    private CompletableFuture<Void> clearNext(ConcurrentLinkedQueue<ChunkJob> jobs, AtomicBoolean failed) {
        ChunkJob job = stopped ? null : jobs.poll();
        if (job == null) return CompletableFuture.completedFuture(null);
        return clearChunk(job).handle((unused, error) -> {
            if (error != null && !stopped) {
                failed.set(true);
                plugin.getLogger().log(Level.SEVERE, "场地清理失败：" + job.arena.world.getName()
                        + " 区块 " + job.x + ", " + job.z, error);
            }
            return null;
        }).thenCompose(unused -> clearNext(jobs, failed));
    }

    private CompletableFuture<Void> clearChunk(ChunkJob job) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Arena arena = job.arena;
        arena.world.getChunkAtAsync(job.x, job.z).whenComplete((chunk, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }
            if (stopped) {
                result.complete(null);
                return;
            }
            int minX = Math.max(arena.minX, job.x << 4);
            int maxX = Math.min(arena.maxX, (job.x << 4) + 15);
            int minZ = Math.max(arena.minZ, job.z << 4);
            int maxZ = Math.min(arena.maxZ, (job.z << 4) + 15);
            int width = maxX - minX + 1;
            int depth = maxZ - minZ + 1;
            int total = width * depth * (arena.maxY - arena.minY + 1);
            int[] cursor = {0};
            try {
                ScheduledTask scheduled = Bukkit.getRegionScheduler().runAtFixedRate(plugin, arena.world, job.x, job.z, task -> {
                    if (stopped) {
                        finish(task, result, null);
                        return;
                    }
                    try {
                        if (cursor[0] == 0) removeCrystals(chunk, arena);
                        int end = Math.min(total, cursor[0] + BLOCKS_PER_TICK);
                        while (cursor[0] < end) {
                            int index = cursor[0]++;
                            int x = minX + index % width;
                            int z = minZ + (index / width) % depth;
                            int y = arena.minY + index / (width * depth);
                            Block block = arena.world.getBlockAt(x, y, z);
                            Material type = block.getType();
                            if (!type.isAir() && type != Material.BEDROCK) block.setType(Material.AIR, false);
                        }
                        if (cursor[0] == total) finish(task, result, null);
                    } catch (Exception exception) {
                        finish(task, result, exception);
                    }
                }, 1, 1);
                chunkTasks.add(scheduled);
                if (stopped) scheduled.cancel();
            } catch (Exception exception) {
                result.completeExceptionally(exception);
            }
        });
        return result;
    }

    private void finish(ScheduledTask task, CompletableFuture<Void> result, Exception error) {
        task.cancel();
        chunkTasks.remove(task);
        if (error == null) result.complete(null);
        else result.completeExceptionally(error);
    }

    private void removeCrystals(Chunk chunk, Arena arena) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof EnderCrystal) {
                entity.getScheduler().run(plugin, task -> {
                    if (!stopped && arena.contains(entity.getLocation())) entity.remove();
                }, null);
            }
        }
    }

    private void broadcast(String message) {
        String text = ChatUtils.getPrefix() + me.yozupi.utils.MessageUtils.withColor(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(plugin, task -> {
                if (!stopped) player.sendMessage(text);
            }, null);
        }
    }

    public synchronized void stop() {
        stopped = true;
        if (clockTask != null) clockTask.cancel();
        if (countdownTask != null) countdownTask.cancel();
        chunkTasks.forEach(ScheduledTask::cancel);
        chunkTasks.clear();
    }

    private record ChunkJob(Arena arena, int x, int z) {
    }

    private record Arena(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        boolean contains(Location location) {
            return location.getWorld().equals(world)
                    && location.getBlockX() >= minX && location.getBlockX() <= maxX
                    && location.getBlockY() >= minY && location.getBlockY() <= maxY
                    && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
        }
    }
}

package me.yozupi.listener.impl;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.yozupi.CrystalLeader;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiPhase implements Listener {
    private static final double EPSILON = 0.0001;
    private final Map<UUID, State> states = new ConcurrentHashMap<>();

    public AntiPhase() {
        Bukkit.getGlobalRegionScheduler().run(CrystalLeader.INSTANCE, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) watch(player);
        });
    }

    private void watch(Player player) {
        State state = new State();
        if (states.putIfAbsent(player.getUniqueId(), state) != null) return;
        state.task = player.getScheduler().runAtFixedRate(CrystalLeader.INSTANCE, task -> check(player, state), () -> states.remove(player.getUniqueId(), state), 1, 1);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        watch(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        State state = states.remove(event.getPlayer().getUniqueId());
        if (state != null && state.task != null) state.task.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL || player.isDead() || player.getGameMode() == GameMode.SPECTATOR)
            return;
        Collision result = collision(event.getTo().getWorld(), bodyAt(player, event.getTo(), true));
        if (result == Collision.BLOCKED) {
            event.setCancelled(true);
            return;
        }
        State state = states.get(player.getUniqueId());
        if (state != null) {
            if (collision(event.getFrom().getWorld(), bodyAt(player, event.getFrom(), false)) == Collision.CLEAR) {
                state.safe = event.getFrom().clone();
            }
            state.pearlArrival = true;
        }
    }

    private void check(Player player, State state) {
        if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
            state.safe = null;
            return;
        }
        if (state.teleporting) return;
        Location current = player.getLocation();
        BoundingBox body = bodyAt(player, current, state.pearlArrival);
        Collision result = collision(current.getWorld(), body);
        if (result == Collision.CLEAR) {
            state.pearlArrival = false;
            state.safe = current.clone();
            return;
        }
        if (result == Collision.UNKNOWN) return;
        Location target = null;
        if (state.pearlArrival && state.safe != null && state.safe.getWorld().equals(current.getWorld())
                && collision(current.getWorld(), bodyAt(player, state.safe, true)) == Collision.CLEAR) {
            target = state.safe.clone();
        }
        if (target == null) {
            target = findVerticalEscape(player, current, state.pearlArrival);
        }
        if (target != null) {
            target.setYaw(current.getYaw());
            target.setPitch(current.getPitch());
            state.teleporting = true;
            player.setVelocity(new Vector());
            player.teleportAsync(target, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    .whenComplete((success, error) -> {
                        player.getScheduler().run(CrystalLeader.INSTANCE, task -> {
                            try {
                                if (Boolean.TRUE.equals(success) && error == null) {
                                    player.setVelocity(new Vector());
                                }
                            } finally {
                                state.teleporting = false;
                            }
                        }, () -> state.teleporting = false);
                    });
        }
    }

    private Location findVerticalEscape(Player player, Location current, boolean pearl) {
        for (int step = 1; step <= 12; step++) {
            Location candidate = current.clone().add(0, step * 0.25, 0);
            Collision result = collision(current.getWorld(), bodyAt(player, candidate, pearl));
            if (result == Collision.CLEAR) return candidate;
            if (result == Collision.UNKNOWN) return null;
        }
        return null;
    }

    private static BoundingBox bodyAt(Player player, Location destination, boolean pearl) {
        BoundingBox box = player.getBoundingBox();
        Location origin = player.getLocation();
        box.shift(destination.getX() - origin.getX(), destination.getY() - origin.getY(), destination.getZ() - origin.getZ());
        if (pearl && box.getHeight() < 1.8) {
            box.resize(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMinY() + 1.8, box.getMaxZ());
        }
        return box.expand(-EPSILON);
    }

    private static Collision collision(World world, BoundingBox body) {
        if (body.getMinY() < world.getMinHeight() || body.getMaxY() > world.getMaxHeight()) return Collision.BLOCKED;
        int minX = (int) Math.floor(body.getMinX());
        int maxX = (int) Math.floor(body.getMaxX());
        int minZ = (int) Math.floor(body.getMinZ());
        int maxZ = (int) Math.floor(body.getMaxZ());
        int minY = Math.max(world.getMinHeight(), (int) Math.floor(body.getMinY()) - 1);
        int maxY = Math.min(world.getMaxHeight() - 1, (int) Math.floor(body.getMaxY()));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!Bukkit.isOwnedByCurrentRegion(world, x >> 4, z >> 4) || !world.isChunkLoaded(x >> 4, z >> 4))
                    return Collision.UNKNOWN;
                for (int y = minY; y <= maxY; y++) {
                    BoundingBox localBody = body.clone().shift(-x, -y, -z);
                    if (world.getBlockAt(x, y, z).getCollisionShape().overlaps(localBody)) return Collision.BLOCKED;
                }
            }
        }
        return Collision.CLEAR;
    }

    private enum Collision {
        CLEAR,
        BLOCKED,
        UNKNOWN
    }

    private static final class State {
        private Location safe;
        private boolean pearlArrival;
        private volatile boolean teleporting;
        private ScheduledTask task;
    }
}

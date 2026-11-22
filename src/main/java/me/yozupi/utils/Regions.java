package me.yozupi.utils;

import me.yozupi.CrystalLeader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Regions {
    private final Location from;
    private final Location to;

    public Regions(Location from, Location to) {
        this.from = new Location(from.getWorld(),
                Math.min(from.getBlockX(), to.getBlockX()),
                Math.min(from.getBlockY(), to.getBlockY()),
                Math.min(from.getBlockZ(), to.getBlockZ()));

        this.to = new Location(to.getWorld(),
                Math.max(from.getBlockX(), to.getBlockX()),
                Math.max(from.getBlockY(), to.getBlockY()),
                Math.max(from.getBlockZ(), to.getBlockZ()));
    }

    public boolean isInArea(Location loc) {
        if (!loc.getWorld().equals(from.getWorld())) {
            return false;
        }

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= from.getBlockX() && x <= to.getBlockX()
                && y >= from.getBlockY() && y <= to.getBlockY()
                && z >= from.getBlockZ() && z <= to.getBlockZ();
    }

    public static boolean isInPVP(Player player) {
        return isInPvpArea(player, "Regions.AnyPvp.pvpArea1") || isInPvpArea(player, "Regions.AnyPvp.pvpArea2");
    }

    private static boolean isInPvpArea(Player player, String path) {
        World world = Bukkit.getWorld(CrystalLeader.configString(path + ".world"));

        Regions area = new Regions(
                new Location(
                        world,
                        CrystalLeader.configInt(path + ".from.x"),
                        CrystalLeader.configInt(path + ".from.y"),
                        CrystalLeader.configInt(path + ".from.z")
                ),
                new Location(
                        world,
                        CrystalLeader.configInt(path + ".to.x"),
                        CrystalLeader.configInt(path + ".to.y"),
                        CrystalLeader.configInt(path + ".to.z")
                )
        );

        return area.isInArea(player.getLocation());
    }
}

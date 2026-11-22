package me.yozupi.tasks;

import me.yozupi.CrystalLeader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CrystalCleaner implements Runnable {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH");

    @Override
    public void run() {
        String hour = dateFormat.format(new Date());
        if (!"12".equals(hour) && !"23".equals(hour)) {
            return;
        }

        ConfigurationSection arenaClear = CrystalLeader.INSTANCE.getConfig()
                .getConfigurationSection("ArenaClear");
        if (arenaClear == null) {
            return;
        }

        for (String key : arenaClear.getKeys(false)) {
            String path = "ArenaClear." + key + ".";
            World world = Bukkit.getWorld(CrystalLeader.configString(path + "world"));
            if (world == null) {
                CrystalLeader.INSTANCE.getLogger().warning("CrystalCleaner: unknown world for " + key);
                continue;
            }

            Location from = new Location(
                    world,
                    CrystalLeader.configInt(path + "from.x"),
                    CrystalLeader.configInt(path + "from.y"),
                    CrystalLeader.configInt(path + "from.z")
            );
            Location to = new Location(
                    world,
                    CrystalLeader.configInt(path + "to.x"),
                    CrystalLeader.configInt(path + "to.y"),
                    CrystalLeader.configInt(path + "to.z")
            );

            clearEndCrystals(world, from, to);
        }
    }

    private void clearEndCrystals(World world, Location from, Location to) {
        int minX = Math.min(from.getBlockX(), to.getBlockX());
        int minY = Math.min(from.getBlockY(), to.getBlockY());
        int minZ = Math.min(from.getBlockZ(), to.getBlockZ());
        int maxX = Math.max(from.getBlockX(), to.getBlockX());
        int maxY = Math.max(from.getBlockY(), to.getBlockY());
        int maxZ = Math.max(from.getBlockZ(), to.getBlockZ());

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof EnderCrystal)) {
                continue;
            }
            Location loc = entity.getLocation();
            if (loc.getX() >= minX && loc.getX() <= maxX
                    && loc.getY() >= minY && loc.getY() <= maxY
                    && loc.getZ() >= minZ && loc.getZ() <= maxZ) {
                entity.remove();
            }
        }
    }
}

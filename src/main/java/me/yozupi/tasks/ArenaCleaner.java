package me.yozupi.tasks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.yozupi.CrystalLeader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ArenaCleaner implements Runnable {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH");

    @Override
    public void run() {
        String hour = dateFormat.format(new Date());
        if (!"12".equals(hour) && !"23".equals(hour)) {
            return;
        }

        ConfigurationSection arenaClear = CrystalLeader.INSTANCE.getConfig().getConfigurationSection("ArenaClear");
        if (arenaClear == null) {
            return;
        }

        for (String key : arenaClear.getKeys(false)) {
            String path = "ArenaClear." + key + ".";
            World bukkitWorld = Bukkit.getWorld(CrystalLeader.configString(path + "world"));

            int minX = Math.min(CrystalLeader.configInt(path + "from.x"), CrystalLeader.configInt(path + "to.x"));
            int minY = Math.min(CrystalLeader.configInt(path + "from.y"), CrystalLeader.configInt(path + "to.y"));
            int minZ = Math.min(CrystalLeader.configInt(path + "from.z"), CrystalLeader.configInt(path + "to.z"));
            int maxX = Math.max(CrystalLeader.configInt(path + "from.x"), CrystalLeader.configInt(path + "to.x"));
            int maxY = Math.max(CrystalLeader.configInt(path + "from.y"), CrystalLeader.configInt(path + "to.y"));
            int maxZ = Math.max(CrystalLeader.configInt(path + "from.z"), CrystalLeader.configInt(path + "to.z"));

            clearArena(bukkitWorld, minX, minY, minZ, maxX, maxY, maxZ, CrystalLeader.configStringList(path + "ExceptBlocks"));
        }
    }

    private void clearArena(World bukkitWorld, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, List<String> exceptBlockNames) {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        Set<BlockType> exceptTypes = exceptBlockNames.stream().map(BlockTypes::get).filter(Objects::nonNull).collect(Collectors.toSet());

        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).limitUnlimited().build()) {
            CuboidRegion region = new CuboidRegion(weWorld, BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
            Mask clearMask = exceptTypes.isEmpty() ? Masks.alwaysTrue() : Masks.negate(new BlockTypeMask(session, exceptTypes));

            session.replaceBlocks(region, clearMask, BlockTypes.AIR.getDefaultState());
            session.flushQueue();

            CrystalLeader.INSTANCE.getLogger().info(
                    "ArenaCleaner: cleared " + region.getWidth() + "x" + region.getHeight() + "x" + region.getLength() + " in " + bukkitWorld.getName()
            );
        } catch (Exception exception) {
            CrystalLeader.INSTANCE.getLogger().warning(
                    "ArenaCleaner: failed to clear arena in " + bukkitWorld.getName() + ": " + exception.getMessage()
            );
        }
    }
}

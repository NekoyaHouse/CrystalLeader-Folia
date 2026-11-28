package me.yozupi.command.impl;

import me.yozupi.CrystalLeader;
import me.yozupi.command.MyCommand;
import me.yozupi.utils.ChatUtils;
import me.yozupi.utils.Regions;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RkitCommand extends MyCommand {
    public RkitCommand() {
        super("rkit", "Teleports you to kit creator.");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (Regions.isInPVP(player)) {
            sender.sendMessage(ChatUtils.getPrefix() + ChatUtils.cantUseThisInPvp());
            return true;
        }

        Location destination = new Location(CrystalLeader.INSTANCE.getServer().getWorld(CrystalLeader.configString("Regions.kitCreator.world")), CrystalLeader.configInt("Regions.kitCreator.x"), CrystalLeader.configInt("Regions.kitCreator.y"), CrystalLeader.configInt("Regions.kitCreator.z"), -90.0f, 0.0f);

        player.teleportAsync(destination).thenAccept(success -> {
            if (!success) return;
            player.sendMessage(ChatUtils.getPrefix() + ChatColor.BLUE + "您已传送到kit制作室");
        });

        return true;
    }
}

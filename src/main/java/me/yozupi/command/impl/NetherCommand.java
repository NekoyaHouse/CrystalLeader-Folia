package me.yozupi.command.impl;

import me.yozupi.command.MyCommand;
import me.yozupi.utils.ChatUtils;
import me.yozupi.utils.Regions;
import me.yozupi.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NetherCommand extends MyCommand {

    public NetherCommand() {
        super("nether", "Teleport you to the nether.");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (Regions.isInPVP(player)) {
            sender.sendMessage(ChatUtils.getPrefix() + ChatUtils.cantUseThisInPvp());
            return true;
        }

        player.teleportAsync(new Location(Utils.NETHER, 0.5, 70, 0.5));

        return true;
    }

}

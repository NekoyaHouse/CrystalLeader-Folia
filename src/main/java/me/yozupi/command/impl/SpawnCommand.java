package me.yozupi.command.impl;

import me.yozupi.command.MyCommand;
import me.yozupi.utils.ChatUtils;
import me.yozupi.utils.Regions;
import me.yozupi.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends MyCommand {

    public SpawnCommand() {
        super("spawn", "Teleports you to the spawn.");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (Regions.isInPVP(player)) {
            sender.sendMessage(ChatUtils.getPrefix() + ChatUtils.cantUseThisInPvp());
            return true;
        }

        player.teleportAsync(new Location(Utils.OVERWORLD, 0.5, 10, 0.5, -90.0f, 0.0f));

        return true;
    }

}

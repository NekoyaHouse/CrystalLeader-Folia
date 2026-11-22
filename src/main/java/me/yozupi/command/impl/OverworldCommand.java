package me.yozupi.command.impl;

import me.yozupi.command.MyCommand;
import me.yozupi.utils.ChatUtils;
import me.yozupi.utils.Regions;
import me.yozupi.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OverworldCommand extends MyCommand {

    public OverworldCommand() {
        super("overworld", "Teleports you to the overworld.");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (Regions.isInPVP(player)) {
            sender.sendMessage(ChatUtils.getPrefix() + ChatUtils.cantUseThisInPvp());
            return true;
        }

        player.teleportAsync(Utils.OVERWORLD.getSpawnLocation().add(0.5, 0, 0.5));

        return true;
    }

}

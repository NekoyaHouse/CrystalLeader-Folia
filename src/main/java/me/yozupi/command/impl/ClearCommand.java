package me.yozupi.command.impl;

import me.yozupi.command.MyCommand;
import me.yozupi.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearCommand extends MyCommand {

    public ClearCommand() {
        super("clear", "Clear you own inventory");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        player.getInventory().remove(Material.AIR);
        player.getInventory().clear();

        player.sendMessage(ChatUtils.PREFIX + ChatColor.RED + "您的背包已经清空!");

        return true;
    }

}

package me.yozupi.command.impl;

import me.yozupi.command.MyCommand;
import me.yozupi.listener.impl.Player;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DonateCommand extends MyCommand {

    public DonateCommand() {
        super("donate", "donate");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        sender.sendMessage(
                ChatColor.YELLOW + "===========[赞助]===========",
                "服主QQ: 3196594300 <<找我赞助:3",
                "赞助金额满50及以上可以找服主自定义一个前缀称号",
                ChatColor.YELLOW + "===========[赞助]==========="
        );

        return true;
    }

}

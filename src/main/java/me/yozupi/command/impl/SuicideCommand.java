package me.yozupi.command.impl;

import me.yozupi.command.MyCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuicideCommand extends MyCommand {

    public SuicideCommand() {
        super("suicide", "Kills you.");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        player.setHealth(0);

        return true;
    }

}

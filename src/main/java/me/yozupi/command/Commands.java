package me.yozupi.command;

import me.yozupi.command.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

public class Commands {
    public static void register() {
        add(new ClearCommand());
        add(new CleanCommand());
        add(new DonateCommand());
        add(new NetherCommand());
        add(new RkitCommand());
        add(new SpawnCommand());
        add(new SuicideCommand());
    }

    private static void add(Command command) {
        Bukkit.getCommandMap().register("crystalleader", command);
    }
}

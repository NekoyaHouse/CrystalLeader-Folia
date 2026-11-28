package me.yozupi.command.impl;

import me.yozupi.CrystalLeader;
import me.yozupi.command.MyCommand;
import me.yozupi.tasks.ArenaClearer;
import me.yozupi.utils.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CleanCommand extends MyCommand {
    public CleanCommand() {
        super("clean", "清理配置中的场地，可指定倒计时秒数", "/<command> [倒计时秒数]");
        setPermission("crystalleader.clean");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        if (args.length > 1) return false;
        int seconds = 0;
        if (args.length == 1) {
            try {
                seconds = Integer.parseInt(args[0]);
                if (seconds < 0 || seconds > 86400) throw new NumberFormatException();
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatUtils.getPrefix() + ChatUtils.message("clean.invalidSeconds", "&c请输入 0 到 86400 之间的整数秒数喵～"));
                return true;
            }
        }

        ArenaClearer clearer = CrystalLeader.INSTANCE.getArenaClearer();
        ArenaClearer.ClearResult result = clearer == null
                ? ArenaClearer.ClearResult.UNAVAILABLE : clearer.clear(seconds);
        String message = switch (result) {
            case STARTED ->
                    seconds == 0 ? ChatUtils.message("clean.started", "&a收到！这就开始打扫场地喵～") : ChatUtils.message("clean.scheduled", "&a记好啦，{seconds} 秒后开始打扫场地喵～", seconds);
            case BUSY -> ChatUtils.message("clean.busy", "&e已经在准备或打扫啦，不要催小猫喵～");
            case UNAVAILABLE ->
                    ChatUtils.message("clean.unavailable", "&c找不到可以打扫的场地喵，请检查 ArenaClear 配置和世界加载状态～");
        };
        sender.sendMessage(ChatUtils.getPrefix() + message);
        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("0", "5", "30", "60").stream()
                    .filter(value -> value.startsWith(args[0])).toList();
        }
        return List.of();
    }
}

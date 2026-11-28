package me.yozupi.command.impl;

import me.yozupi.CrystalLeader;
import me.yozupi.command.MyCommand;
import me.yozupi.tasks.ArenaClearer;
import me.yozupi.utils.ChatUtils;
import me.yozupi.utils.MessageUtils;
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
                sender.sendMessage(ChatUtils.getPrefix() + MessageUtils.withColor("&c倒计时必须是 0 到 86400 之间的整数（秒）。"));
                return true;
            }
        }

        ArenaClearer clearer = CrystalLeader.INSTANCE.getArenaClearer();
        ArenaClearer.ClearResult result = clearer == null
                ? ArenaClearer.ClearResult.UNAVAILABLE : clearer.clear(seconds);
        String message = switch (result) {
            case STARTED -> seconds == 0 ? "&a已开始清理场地。" : "&a已设置清场倒计时：" + seconds + " 秒。";
            case BUSY -> "&e场地正在倒计时或清理中，请勿重复执行。";
            case UNAVAILABLE -> "&c场地清理不可用，请检查 ArenaClear 配置及世界是否已加载。";
        };
        sender.sendMessage(ChatUtils.getPrefix() + MessageUtils.withColor(message));
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

package com.mengcraft.antispam;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

/**
 * Created on 16-10-11.
 */
public class SpamCommand implements CommandExecutor {

    private final AntiSpam spam;

    public SpamCommand(AntiSpam spam) {
        this.spam = spam;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command i, String label, String[] j) {
        Iterator<String> it = ImmutableList.copyOf(j).iterator();
        if (it.hasNext()) {
            return execute(sender, it.next(), it);
        } else {
            sender.sendMessage(ChatColor.GOLD + "/spam add abc 添加abc到脏话列表");
            sender.sendMessage(ChatColor.GOLD + "/spam remove abc 从脏话列表移除abc");
            sender.sendMessage(ChatColor.GOLD + "/spam list 查看已定义脏话");
            sender.sendMessage(ChatColor.GOLD + "/spam reload 重新读取配置项");
        }
        return false;
    }

    private boolean execute(CommandSender sender, String next, Iterator<String> it) {
        if (AntiSpam.eq(next, "list")) {
            sender.sendMessage(ChatColor.GOLD + "> 脏话列表");
            spam.sendFilterMessage(sender);
            return true;
        } else if (AntiSpam.eq(next, "reload")) {
            spam.reload();
            SpamListener.get(spam).reload();
            sender.sendMessage(ChatColor.GOLD + "操作已完成，部分内容需重启插件重载");
            return true;
        } else if (AntiSpam.eq(next, "add")) {
            if (it.hasNext()) return add(sender, it.next());
        } else if (AntiSpam.eq(next, "remove")) {
            if (it.hasNext()) return remove(sender, it.next());
        }
        return false;
    }

    private boolean remove(CommandSender sender, String next) {
        boolean remove = spam.removeFilter(next);
        if (remove) sender.sendMessage(ChatColor.GOLD + "操作已完成");
        return remove;
    }

    private boolean add(CommandSender sender, String next) {
        boolean add = spam.addFilter(next);
        if (add) sender.sendMessage(ChatColor.GOLD + "操作已完成");
        return add;
    }

}

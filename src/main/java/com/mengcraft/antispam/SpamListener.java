package com.mengcraft.antispam;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.mengcraft.antispam.AntiSpam.nil;

/**
 * Created on 16-10-11.
 */
public class SpamListener implements Listener {

    private final Map<UUID, Integer> time = new HashMap<>();
    private final Map<UUID, String> message = new HashMap<>();

    private List<String> whiteList;
    private int limit;
    private int wait;
    private int commandWait;
    private boolean notNotify;
    private boolean debug;

    private final AntiSpam spam;

    private SpamListener(AntiSpam spam) {
        this.spam = spam;
        reload();
    }

    public void reload() {
        wait = spam.getConfig().getInt("config.chatWait");
        limit = spam.getConfig().getInt("config.chatLimit");
        commandWait = spam.getConfig().getInt("config.commandWait");
        notNotify = spam.getConfig().getBoolean("config.notNotify");
        whiteList = spam.getConfig().getStringList("config.commandWhiteList");
        debug = spam.getConfig().getBoolean("debug");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handle(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("spam.bypass")) {
            ;
        } else check(event);
    }

    public void check(AsyncPlayerChatEvent event) {
        if (spam(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "请不要刷屏或发送重复消息哦");
        } else if (check(event.getPlayer(), event.getMessage())) {
            if (notNotify) {
                Set<Player> set = event.getRecipients();
                set.clear();
                set.add(event.getPlayer());
                event.setMessage(event.getMessage() + "§r§r");
                if (debug) spam.getLogger().info("DEBUG #1");
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "请不要发送含有屏蔽字的消息");
            }
        } else {
            time.put(event.getPlayer().getUniqueId(), AntiSpam.now());
            message.put(event.getPlayer().getUniqueId(), event.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handle(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("spam.bypass")) {
            ;
        } else if (whiteListed(event.getMessage())) {
            ;
        } else check(event);
    }

    private void check(PlayerCommandPreprocessEvent event) {
        if (spam(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "请不要过于频繁使用指令");
        } else if (check(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "您的指令中含有屏蔽字词");
        } else {
            time.put(event.getPlayer().getUniqueId(), AntiSpam.now());
        }
    }

    private boolean spam(Player player) {
        if (time.containsKey(player.getUniqueId()))
            return time.get(player.getUniqueId()) + commandWait > AntiSpam.now();
        return false;
    }

    private boolean whiteListed(String str) {
        for (String s : whiteList)
            if (str.startsWith(s)) return true;
        return false;
    }

    private boolean check(Player p, String i) {
        return spam.check(p, i);
    }

    private boolean spam(Player player, String str) {
        if (time.containsKey(player.getUniqueId())) {
            int latest = time.get(player.getUniqueId());
            int now = AntiSpam.now();
            if (latest + wait > now) {
                return true;
            }
            if (latest + limit > now) {
                return Similarity.process(message.get(player.getUniqueId()), str).get() > 0.8;
            }
        }
        return false;
    }

    public static SpamListener get(AntiSpam spam) {
        if (nil(instance)) {
            instance = new SpamListener(spam);
        }
        return instance;
    }

    private static SpamListener instance;

}

package com.mengcraft.antispam;

import com.mengcraft.antispam.entity.DWhitelist;
import lombok.val;
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
import java.util.regex.Pattern;

import static com.mengcraft.antispam.AntiSpam.nil;

/**
 * Created on 16-10-11.
 */
public class SpamListener implements Listener {

    private final Map<UUID, Integer> time = new HashMap<>();
    private final Map<UUID, String> message = new HashMap<>();

    private Pattern whiteList;
    private int length;
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
        length = spam.getConfig().getInt("config.chatLengthLimit");
        wait = spam.getConfig().getInt("config.chatWait");
        limit = spam.getConfig().getInt("config.chatLimit");
        commandWait = spam.getConfig().getInt("config.commandWait");
        notNotify = spam.getConfig().getBoolean("config.notNotify");
        debug = spam.getConfig().getBoolean("debug");
        val l = spam.getConfig().getStringList("config.commandWhiteList");
        if (spam.remoteEnabled) {
            spam.getDatabase().find(DWhitelist.class).findList().forEach(i -> l.add(i.getLine()));
        }
        whiteList = buildRegPattern(l);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handle(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("spam.bypass")) return;

        String message = event.getMessage();
        val p = event.getPlayer();

        if (length > 0 && message.length() > length) {// TODO Optional cutoff or notify
            event.setMessage(message = message.substring(0, length));
        }

        if (spam(p, message)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "请不要刷屏或发送重复消息哦");
        } else if (check(p, message)) {
            if (notNotify) {
                Set<Player> set = event.getRecipients();
                set.clear();
                set.add(p);
                event.setMessage(message + "§r§r");
                if (debug) spam.getLogger().info("DEBUG #1");
            } else {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED + "请不要发送含有屏蔽字的消息");
            }
        } else {
            time.put(p.getUniqueId(), AntiSpam.now());
            this.message.put(p.getUniqueId(), message);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void handle(PlayerCommandPreprocessEvent event) {
        val p = event.getPlayer();
        if (p.hasPermission("spam.bypass") || whiteList.matcher(event.getMessage()).matches()) return;
        if (spam(p)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "请不要过于频繁使用指令");
        } else if (check(p, event.getMessage())) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.RED + "您的指令中含有屏蔽字词");
        } else {
            time.put(p.getUniqueId(), AntiSpam.now());
        }
    }

    private boolean spam(Player player) {
        if (time.containsKey(player.getUniqueId()))
            return time.get(player.getUniqueId()) + commandWait > AntiSpam.now();
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

    /**
     * 给其他插件用的，别删。
     *
     * @return instance of this listener
     */
    public static SpamListener getInstance() {
        if (nil(instance)) throw new IllegalStateException("null");
        return instance;
    }

    public static Pattern buildRegPattern(List<String> list) {
        if (nil(list) || list.isEmpty()) return null;
        val b = new StringBuilder();
        val i = list.iterator();
        while (i.hasNext()) {
            val l = i.next();
            if (!(nil(l) || l.isEmpty())) {
                if (!(l.charAt(0) == '/')) b.append('/');
                b.append(l);
                if (i.hasNext()) b.append('|');
            }
        }
        return Pattern.compile("^(" + b + ")(\\s+.*|$)");
    }

    private static SpamListener instance;

}

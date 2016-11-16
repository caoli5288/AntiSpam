package com.mengcraft.antispam;

import com.mengcraft.antispam.filter.FilterChain;
import com.mengcraft.simpleorm.DatabaseException;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * GPLv2 license
 */

public class AntiSpam extends JavaPlugin {

    private FilterChain filter;
    private Set<String> raw;
    private boolean remote;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        List<String> list = getConfig().getStringList("config.dirtyList");
        raw = new HashSet<>(list);

        if (getConfig().getBoolean("config.useRemoteList")) {
            Plugin plugin = getServer().getPluginManager().getPlugin("SimpleORM");
            if (plugin == null) {
                getLogger().log(Level.SEVERE, "没有发现SimpleORM，远程列表无法开启");
            } else {
                EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
                if (db.isNotInitialized()) {
                    db.define(Bean.class);
                    try {
                        db.initialize();
                        db.install();
                        db.reflect();

                        raw.addAll(db.find(Bean.class)
                                .findList()
                                .stream()
                                .map(Bean::getLine)
                                .collect(Collectors.toList()));

                        remote = true;
                    } catch (DatabaseException e) {
                        getLogger().log(Level.SEVERE, "无法连接到数据库，请检查配置文件");
                    }
                }
            }
        }


        filter = FilterChain.build(list);

        String[] lines = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(lines);

        getServer().getPluginManager().registerEvents(new SpamListener(this), this);
        getServer().getPluginCommand("spam").setExecutor(new SpamCommand(this));

        Metrics.start(this);
    }

    public void reload() {
        List<String> list = getConfig().getStringList("config.dirtyList");
        raw = new HashSet<>(list);
        if (remote) {
            raw.addAll(getDatabase().find(Bean.class)
                    .findList()
                    .stream()
                    .map(Bean::getLine)
                    .collect(Collectors.toList()));
        }
        filter = FilterChain.build(list);
    }

    public boolean check(String i) {
        return filter.check(i);
    }

    public boolean removeFilter(String in) {
        boolean remove = raw.remove(in);
        if (remove) {
            save();
            filter = FilterChain.build(raw);
        }
        return remove;
    }

    public boolean addFilter(String in) {
        boolean add = raw.add(in);
        if (add) {
            filter.add(in);
            save();
        }
        return add;
    }

    private void save() {
        getConfig().set("config.dirtyList", new ArrayList<>(raw));
        saveConfig();
    }

    protected Set<String> getRaw() {
        return raw;
    }

    public static int unixTime() {
        return toIntExact(System.currentTimeMillis() / 1000);
    }

    public static int toIntExact(long value) {
        if ((int) value != value) {
            throw new ArithmeticException("integer overflow");
        }
        return (int) value;
    }

    public static boolean eq(Object i, Object j) {
        return i == j || i.equals(j);
    }

}

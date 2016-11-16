package com.mengcraft.antispam;

import com.mengcraft.antispam.entity.Dirty;
import com.mengcraft.antispam.entity.DirtyRecord;
import com.mengcraft.antispam.filter.FilterChain;
import com.mengcraft.simpleorm.DatabaseException;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * GPLv2 license
 */

public class AntiSpam extends JavaPlugin {

    private FilterChain filter;
    private Set<String> raw;

    private boolean remoteEnabled;
    private boolean logging;

    private ExecutorService pool;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        logging = getConfig().getBoolean("config.log");

        List<String> list = getConfig().getStringList("config.dirtyList");
        raw = new HashSet<>(list);

        if (getConfig().getBoolean("config.useRemote")) {
            Plugin plugin = getServer().getPluginManager().getPlugin("SimpleORM");
            if (plugin == null) {
                getLogger().log(Level.SEVERE, "没有发现SimpleORM，远程列表无法开启");
            } else {
                EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
                if (db.isNotInitialized()) {
                    db.define(Dirty.class);
                    db.define(DirtyRecord.class);
                    try {
                        db.initialize();
                        db.install();
                        db.reflect();

                        raw.addAll(db.find(Dirty.class)
                                .findList()
                                .stream()
                                .map(Dirty::getLine)
                                .collect(Collectors.toList()));

                        remoteEnabled = true;
                    } catch (DatabaseException e) {
                        getLogger().log(Level.SEVERE, "无法连接到数据库，请检查配置文件 <- " + e.getMessage());
                    }
                }
            }
        }

        if (logging && remoteEnabled) {
            pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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
        if (remoteEnabled) {
            raw.addAll(getDatabase().find(Dirty.class)
                    .findList()
                    .stream()
                    .map(Dirty::getLine)
                    .collect(Collectors.toList()));
        }
        filter = FilterChain.build(list);
    }

    @Override
    public void onDisable() {
        if (pool != null) {
            pool.shutdown();
            try {
                pool.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                getLogger().log(Level.SEVERE, "", e);
            }
        }
    }

    public boolean check(Player p, String i) {
        boolean result = filter.check(i);
        if (result && logging) {
            if (remoteEnabled) {
                DirtyRecord record = new DirtyRecord();
                record.setPlayer(p.getName());
                record.setChat(i);
                pool.execute(() -> getDatabase().save(record));
            } else {
                getLogger().info(p.getName() + "|" + i);
            }
        }
        return result;
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

    public boolean isLogging() {
        return logging;
    }

    public boolean isRemoteEnabled() {
        return remoteEnabled;
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

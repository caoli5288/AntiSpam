package com.mengcraft.antispam;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * GPLv2 license
 */

public class AntiSpam extends JavaPlugin {

    private final Set<String> dirtySet = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        for (String line : getConfig().getStringList("config.dirtyList")) {
            dirtySet.add(line.toUpperCase());
        }

        String[] lines = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(lines);

        try {
            new Metrics(this).start();
        } catch (IOException e) {
            getLogger().warning(e.toString());
        }

        getServer().getPluginManager().registerEvents(new SpamListener(this), this);
        getServer().getPluginCommand("spam").setExecutor(new SpamCommand(this));
    }

    public Set<String> getDirtySet() {
        return dirtySet;
    }

    public void reload() {
        reloadConfig();
        if (!dirtySet.isEmpty()) dirtySet.clear();
        for (String line : getConfig().getStringList("config.dirtyList")) {
            dirtySet.add(line.toUpperCase());
        }
    }

    public void save() {
        getConfig().set("config.dirtyList", new ArrayList<>(dirtySet));
        saveConfig();
    }

    public static boolean eq(Object i, Object j) {
        return i == j || (i != null && i.equals(j));
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

}

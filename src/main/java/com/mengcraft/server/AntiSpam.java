package com.mengcraft.server;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.*;

/**
 * GPLv2 license
 */

public class AntiSpam extends JavaPlugin {

	@Override
	public void onLoad() {
		saveDefaultConfig();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new AntiListener(), this);
		getServer().getPluginCommand("spam").setExecutor(new Commander());
		String[] strings = {
				ChatColor.GREEN + "梦梦家高性能服务器出租"
				, ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com"
		};
		Bukkit.getConsoleSender().sendMessage(strings);
		try {
			new Metrics(this).start();
		} catch (IOException e) {
			getLogger().warning("Can not connect to mcstats.org!");
		}
	}

	private class Commander implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length < 1) {
				sendInfo(sender);
			} else if (args.length < 2) {
				switch (args[0]) {
				case "list":
					sendList(sender);
					break;
				case "reload":
					sendReload(sender);
					break;
				default:
					sendInfo(sender);
					break;
				}
			} else if (args.length < 3) {
				switch (args[0]) {
				case "add":
					setDirtyList(sender, args[1], true);
					break;
				case "remove":
					setDirtyList(sender, args[1], false);
					break;
				default:
					sendInfo(sender);
					break;
				}
			} else {
				sendInfo(sender);
			}
			return true;
		}

		private void sendReload(CommandSender sender) {
			reloadConfig();
			sender.sendMessage(ChatColor.GREEN + "[AntiSpam] 重新读取配置文件完毕!");
		}

		private void sendList(CommandSender sender) {
			List<String> dirtyList = getConfig().getStringList("config.dirtyList");
			dirtyList.add(0, "脏话列表:");
			sender.sendMessage(dirtyList.toArray(new String[dirtyList.size()]));
		}

		private void sendInfo(CommandSender sender) {
			String[] message = {
					ChatColor.GOLD + "/spam add abc           将 abc 添加到脏话列表",
					ChatColor.GOLD + "/spam remove abc        将 abc 从脏话列表移除",
					ChatColor.GOLD + "/spam list              显示脏话列表",
					ChatColor.GOLD + "/spam reload            重新读取配置文件"
			};
			sender.sendMessage(message);
		}

		private void setDirtyList(CommandSender sender, String dirty, boolean isAdd) {
			if (sender.hasPermission("spam.admin")) {
				List<String> dirtyList = getConfig().getStringList("config.dirtyList");
				Set<String> dirtySet = new HashSet<>(dirtyList);
				if (isAdd) {
					if (dirtySet.add(dirty)) {
						String message = ChatColor.GREEN + "将 abc 添加到屏蔽列表成功".replaceAll("abc", dirty);
						sender.sendMessage(message);
						getConfig().set("config.dirtyList", new ArrayList<>(dirtySet));
						saveConfig();
					} else {
						String message = ChatColor.RED + "将 abc 添加到屏蔽列表失败".replaceAll("abc", dirty);
						sender.sendMessage(message);
					}
				} else {
					if (dirtySet.remove(dirty)) {
						String message = ChatColor.GREEN + "将 abc 从屏蔽列表移除成功".replaceAll("abc", dirty);
						sender.sendMessage(message);
						getConfig().set("config.dirtyList", new ArrayList<>(dirtySet));
						saveConfig();
					} else {
						String message = ChatColor.RED + "将 abc 从屏蔽列表移除失败".replaceAll("abc", dirty);
						sender.sendMessage(message);
					}
				}
			}
		}
	}

	private class AntiListener implements Listener {

		private final HashMap<String, Long> lastTime;
		private final HashMap<String, String> lastMessage;
		private final Map<String, Long> commandWait;

		public AntiListener() {
			this.commandWait = new HashMap<String, Long>();
			this.lastTime = new HashMap<>();
			this.lastMessage = new HashMap<>();
		}

		@EventHandler(ignoreCancelled = true)
		public void command(PlayerCommandPreprocessEvent event) {
			boolean b = event.getPlayer().hasPermission("spam.anti")
					&& !event.getMessage().startsWith("/spam");
			if (b) {
				String name = event.getPlayer().getName();
				if (isNoCommandTime(name)) {
					String message = ChatColor.RED + "你打命令太频繁了";
					event.getPlayer().sendMessage(message);
					event.setCancelled(true);
				} else if (isDirty(event.getMessage())) {
					String message = ChatColor.RED + "你疑似说脏话了";
					event.getPlayer().sendMessage(message);
					event.setCancelled(true);
				} else {
					lastMessage.put(name, event.getMessage());
					commandWait.put(name, System.currentTimeMillis() / 1000);
				}
			}
		}

		@EventHandler(ignoreCancelled = true)
		public void chat(AsyncPlayerChatEvent event) {
			if (event.getPlayer().hasPermission("spam.anti")) {
				String name = event.getPlayer().getName();
				if (isNoChatTime(name)) {
					String message = ChatColor.RED + "你说话太频繁了";
					event.getPlayer().sendMessage(message);
					event.setCancelled(true);
				} else if (isChatLimitTime(name) && isSimilar(name, event.getMessage())) {
					String message = ChatColor.RED + "你疑似刷屏了";
					event.getPlayer().sendMessage(message);
					event.setCancelled(true);
				} else if (isDirty(event.getMessage())) {
					String message = ChatColor.RED + "你疑似说脏话了";
					event.getPlayer().sendMessage(message);
					event.setCancelled(true);
				} else {
					lastMessage.put(name, event.getMessage());
					lastTime.put(name, System.currentTimeMillis() / 1000);
				}
			}
		}

		private boolean isDirty(String message) {
			List<String> dirtyList = getConfig().getStringList("config.dirtyList");
			boolean isDirty = false;
			for (String dirty : dirtyList) {
				if (!isDirty) {
					isDirty = message.contains(dirty);
				} else {
					break;
				}
			}
			return isDirty;
		}

		private boolean isSimilar(String name, String message) {
			String last = lastMessage.get(name);
			if (last != null) {
				if (last.length() > 1) {
					int size = last.length() / 2;
					String head = last.substring(0, size);
					String tail = last.substring(size);
					return message.contains(head) || message.contains(tail);
				} else
					return message.equals(last);
			}
			return false;
		}

		private boolean isNoCommandTime(String name) {
			return commandWait.get(name) != null && commandWait.get(name) + getConfig().getLong("config.commandWait", 1) > System.currentTimeMillis() / 1000;
		}
		
		private boolean isNoChatTime(String name) {
			/*
			 * long last = lastTime.get(name);long current =
			 * System.currentTimeMillis() / 1000;long delay =
			 * getConfig().getLong("config.chatWait", 1);
			 */
			return lastTime.get(name) != null && lastTime.get(name) + getConfig().getLong("config.chatWait", 1) > System.currentTimeMillis() / 1000;
		}

		private boolean isChatLimitTime(String name) {
			return lastTime.get(name) != null && lastTime.get(name) + getConfig().getLong("config.chatLimit", 10) > System.currentTimeMillis() / 1000;
		}
	}
}

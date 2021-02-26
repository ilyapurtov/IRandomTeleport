package com.ilyapurtov;

import com.ilyapurtov.channel.ChannelManager;
import com.ilyapurtov.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public final class IRandomTeleport extends JavaPlugin {

    private FileConfiguration config;
    private File configFile, messagesFile;
    private Settings settings;
    private ChannelManager channelManager;

    @Override
    public void onEnable() {
        configFile = new File(getDataFolder() + "/config.yml");
        if (!configFile.exists()) saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);

        settings = new Settings();
        settings.load(config);

        messagesFile = new File(getDataFolder() + "/messages.yml");
        if (!messagesFile.exists()) saveResource("messages.yml", false);
        MessageUtil.messages = YamlConfiguration.loadConfiguration(messagesFile);

        if (settings.getRtpCommand() != null) {
            PluginCommand command = null;
            CommandMap map = null;
            try {
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);

                command = c.newInstance(settings.getRtpCommand(), this);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                try {
                    Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                    f.setAccessible(true);

                    map = (CommandMap) f.get(Bukkit.getPluginManager());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (map != null)  map.register(getDescription().getName(), command);
            if (command != null) command.setExecutor(new RandomTeleportCommand(this));
        }

        channelManager = new ChannelManager(this);

        getLogger().info(ChatColor.GREEN + "Plugin enabled!");
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        this.settings.load(config);
        MessageUtil.messages = YamlConfiguration.loadConfiguration(messagesFile);
        this.channelManager = new ChannelManager(this);
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.YELLOW + "Plugin disabled!");
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public Settings getSettings() {
        return settings;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }
}

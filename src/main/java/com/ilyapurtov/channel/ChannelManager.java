package com.ilyapurtov.channel;

import com.ilyapurtov.IRandomTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class ChannelManager {

    public enum ChannelType {DEFAULT, NEARBY_PLAYERS}
    public enum TeleportType {NORMAL, SKY}

    private final IRandomTeleport plugin;
    private final List<Channel> channels = new ArrayList<>();
    private final List<Player> teleportingPlayers = new ArrayList<>();

    public ChannelManager(IRandomTeleport instance) {
        plugin = instance;

        ConfigurationSection channelsSection = plugin.getConfig().getConfigurationSection("channels");
        for (String configName : channelsSection.getKeys(false)) {
            ConfigurationSection s = channelsSection.getConfigurationSection(configName);
            List<TeleportAction> actions = new ArrayList<>();
            for (String action : s.getStringList("actions")) {
                String[] act = action.split(", ");
                actions.add(new TeleportAction(act[0], act[1].split(" && ")));
            }
            Channel channel = new Channel(plugin, configName, s.getString("argument"), ChannelType.valueOf(s.getString("type")), s.getBoolean("default"), s.getString("range.x"), s.getString("range.z"), TeleportType.valueOf(s.getString("teleportType")), s.getStringList("blacklistedBlocks"), s.getString("hunger"), actions, s.getString("nearbyRange"), s.get("cooldown"));
            channels.add(channel);
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onSkyTeleport(EntityDamageEvent e) {
                if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && e.getEntity() instanceof Player) {
                    Player p = (Player) e.getEntity();
                    if (getTeleportingPlayers().contains(p)) {
                        e.setCancelled(true);
                        getTeleportingPlayers().remove(p);
                    }
                }
            }

        }, plugin);
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public List<Player> getTeleportingPlayers() {
        return teleportingPlayers;
    }

    public Channel getChannel(String argument) {
        for (Channel channel : channels) {
            if (channel.getArgument().equalsIgnoreCase(argument)) return channel;
        }
        return null;
    }

    public Channel getDefaultChannel() {
        for (Channel channel : channels) {
            if (channel.isDefault()) return channel;
        }
        return null;
    }

    public static class TeleportAction {

        private final String name;
        private final String[] args;

        TeleportAction(String name, String[] args) {
            this.name = name;
            this.args = args;
        }

        public String getName() {
            return name;
        }

        public String[] getArgs() {
            return args;
        }
    }

    public static class TeleportLocationData {

        private final Location location;
        private final int x, y, z;
        private final Player randomPlayer;

        public TeleportLocationData(Location location, int x, int y, int z, Player randomPlayer) {
            this.location = location;
            this.x = x;
            this.y = y;
            this.z = z;
            this.randomPlayer = randomPlayer;
        }

        public Location getLocation() {
            return location;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public Player getRandomPlayer() {
            return randomPlayer;
        }
    }
}

package com.ilyapurtov;

import com.ilyapurtov.channel.Channel;
import com.ilyapurtov.channel.ChannelManager;
import com.ilyapurtov.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RandomTeleportCommand implements CommandExecutor {

    private final IRandomTeleport plugin;

    public RandomTeleportCommand(IRandomTeleport instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Channel defaultChannel = plugin.getChannelManager().getDefaultChannel();
                if (defaultChannel != null) {
                    try {
                        ChannelManager.TeleportLocationData data = defaultChannel.generateLocation(player, false);
                        player.teleport(data.getLocation());
                        if (data.getRandomPlayer() == null) {
                            Map<String, Object> placeholders = new HashMap<>();
                            placeholders.put("{x}", data.getX());
                            placeholders.put("{y}", data.getY());
                            placeholders.put("{z}", data.getZ());
                            MessageUtil.send(sender, "teleported", placeholders);
                        } else {
                            Location randomPlayerLocation = data.getRandomPlayer().getLocation();
                            Map<String, Object> placeholders = new HashMap<>();
                            placeholders.put("{x}", randomPlayerLocation.getBlockX());
                            placeholders.put("{y}", randomPlayerLocation.getBlockY());
                            placeholders.put("{z}", randomPlayerLocation.getBlockZ());
                            placeholders.put("{player}", data.getRandomPlayer().getName());
                            MessageUtil.send(sender, "nearbyTeleported", placeholders);
                        }
                    } catch (IllegalStateException exc) {
                        String message = exc.getLocalizedMessage();
                        switch (message) {
                            case "MIN_ONLINE":
                                MessageUtil.send(sender, "nearbyMinOnline", Collections.singletonMap("{count}", plugin.getSettings().getNearbyPlayersMinOnline()));
                                break;
                            case "EMPTY_WORLD":
                                MessageUtil.send(sender, "nearbyEmptyWorld");
                                break;
                            case "NO_HUNGER":
                                MessageUtil.send(sender, "noHunger", Collections.singletonMap("{count}", defaultChannel.getHunger()));
                                break;
                            case "NO_PERMISSION":
                                MessageUtil.sendChannelNoPermission(sender, defaultChannel.getConfigName());
                                break;
                            default:
                                if (message.startsWith("COOLDOWN_")) {
                                    String remained = message.replace("COOLDOWN_", "");
                                    MessageUtil.send(sender, "cooldown", Collections.singletonMap("{remained}", remained));
                                }
                                break;
                        }
                    }
                } else {
                    MessageUtil.send(sender, "helpPlayer", Collections.singletonMap("{label}", command.getName()));
                }
            } else {
                MessageUtil.send(sender, "helpOp", Collections.singletonMap("{label}", command.getName()));
            }
        }

        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {

                if (sender.hasPermission("rtp.admin")) {
                    MessageUtil.send(sender, "helpOp", Collections.singletonMap("{label}", command.getName()));
                } else {
                    MessageUtil.send(sender, "helpPlayer", Collections.singletonMap("{label}", command.getName()));
                }

            } else if (args[0].equalsIgnoreCase("reload")) {

                if (sender.hasPermission("rtp.admin")) {
                    plugin.reload();
                    sender.sendMessage(ChatColor.GREEN + "Success!");
                } else {
                    MessageUtil.send(sender, "noPermission");
                }

            } else {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Channel channel = plugin.getChannelManager().getChannel(args[0]);
                    if (channel != null) {
                        try {
                            ChannelManager.TeleportLocationData data = channel.generateLocation(player, false);
                            player.teleport(data.getLocation());
                            if (data.getRandomPlayer() == null) {
                                Map<String, Object> placeholders = new HashMap<>();
                                placeholders.put("{x}", data.getX());
                                placeholders.put("{y}", data.getY());
                                placeholders.put("{z}", data.getZ());
                                MessageUtil.send(sender, "teleported", placeholders);
                            } else {
                                Location randomPlayerLocation = data.getRandomPlayer().getLocation();
                                Map<String, Object> placeholders = new HashMap<>();
                                placeholders.put("{x}", randomPlayerLocation.getBlockX());
                                placeholders.put("{y}", randomPlayerLocation.getBlockY());
                                placeholders.put("{z}", randomPlayerLocation.getBlockZ());
                                placeholders.put("{player}", data.getRandomPlayer().getName());
                                MessageUtil.send(sender, "nearbyTeleported", placeholders);
                            }
                        } catch (IllegalStateException exc) {
                            String message = exc.getLocalizedMessage();
                            switch (message) {
                                case "MIN_ONLINE":
                                    MessageUtil.send(sender, "nearbyMinOnline", Collections.singletonMap("{count}", plugin.getSettings().getNearbyPlayersMinOnline()));
                                    break;
                                case "EMPTY_WORLD":
                                    MessageUtil.send(sender, "nearbyEmptyWorld");
                                    break;
                                case "NO_HUNGER":
                                    MessageUtil.send(sender, "noHunger", Collections.singletonMap("{count}", channel.getHunger()));
                                    break;
                                case "NO_PERMISSION":
                                    MessageUtil.sendChannelNoPermission(sender, channel.getConfigName());
                                    break;
                            }
                        }
                    } else {
                        MessageUtil.send(sender, "channelNotFound");
                    }
                } else {
                    MessageUtil.send(sender, "onlyForPlayers");
                }
            }
        }

        else if (args.length == 2) {
            if (sender.hasPermission("rtp.admin")) {

                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    MessageUtil.send(sender, "playerNotFound");
                    return true;
                }
                Channel channel = plugin.getChannelManager().getChannel(args[0]);
                if (channel != null) {
                    try {
                        ChannelManager.TeleportLocationData data = channel.generateLocation(player, true);
                        player.teleport(data.getLocation());
                        if (data.getRandomPlayer() == null) {
                            Map<String, Object> placeholders = new HashMap<>();
                            placeholders.put("{x}", data.getX());
                            placeholders.put("{y}", data.getY());
                            placeholders.put("{z}", data.getZ());
                            placeholders.put("{player}", player.getName());
                            MessageUtil.send(sender, "teleportedOther", placeholders);
                        } else {
                            Location randomPlayerLocation = data.getRandomPlayer().getLocation();
                            Map<String, Object> placeholders = new HashMap<>();
                            placeholders.put("{x}", randomPlayerLocation.getBlockX());
                            placeholders.put("{y}", randomPlayerLocation.getBlockY());
                            placeholders.put("{z}", randomPlayerLocation.getBlockZ());
                            placeholders.put("{player}", data.getRandomPlayer().getName());
                            placeholders.put("{teleportedPlayer}", player.getName());
                            MessageUtil.send(sender, "nearbyTeleportedOther", placeholders);
                        }
                    } catch (IllegalStateException exc) {
                        String message = exc.getLocalizedMessage();
                        switch (message) {
                            case "MIN_ONLINE":
                                MessageUtil.send(sender, "nearbyMinOnline", Collections.singletonMap("{count}", plugin.getSettings().getNearbyPlayersMinOnline()));
                                break;
                            case "EMPTY_WORLD":
                                MessageUtil.send(sender, "nearbyEmptyWorld");
                                break;
                            case "NO_HUNGER":
                                MessageUtil.send(sender, "noHunger", Collections.singletonMap("{count}", channel.getHunger()));
                                break;
                        }
                    }
                } else {
                    MessageUtil.send(sender, "channelNotFound");
                }

            } else {
                MessageUtil.send(sender, "noPermission");
            }
        }

        return true;
    }

}

package com.ilyapurtov.channel;

import com.ilyapurtov.IRandomTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Channel {

    private final IRandomTeleport plugin;
    private final String configName;
    private final String argument;
    private final ChannelManager.ChannelType type;
    private final boolean default_;
    private final String xRange;
    private final String zRange;
    private final ChannelManager.TeleportType teleportType;
    private final List<String> blacklistedBlocks;
    private final String hunger;
    private final List<ChannelManager.TeleportAction> actions;
    private final String nearbyRange;
    private final int cooldown;
    private final Map<Player, Integer> cooldowns;

    public Channel(IRandomTeleport instance, String configName, String argument, ChannelManager.ChannelType type, boolean default_, String xRange, String zRange, ChannelManager.TeleportType teleportType, List<String> blacklistedBlocks, String hunger, List<ChannelManager.TeleportAction> actions, String nearbyRange, Object cooldown) {

        this.plugin = instance;
        this.configName = configName;
        this.argument = argument;
        this.type = type;
        this.default_ = default_;
        this.xRange = xRange;
        this.zRange = zRange;
        this.teleportType = teleportType;
        this.blacklistedBlocks = blacklistedBlocks == null ? new ArrayList<>() : blacklistedBlocks;
        this.hunger = hunger;
        this.actions = actions == null ? new ArrayList<>() : actions;
        this.nearbyRange = nearbyRange;
        this.cooldown = cooldown == null ? -1 : (int) cooldown;
        this.cooldowns = new HashMap<>();
    }

    public ChannelManager.TeleportLocationData generateLocation(Player player, boolean ignorePermissions) throws IllegalStateException {

        if (!ignorePermissions) {
            if (!player.hasPermission("rtp.allchannels")) {
                if (!player.hasPermission("rtp.channel." + getConfigName())) {
                    throw new IllegalStateException("NO_PERMISSION");
                }
            }
        }

        if (getType().equals(ChannelManager.ChannelType.DEFAULT)) {

            String[] xr = getXRange().split(" ! ");
            int x = ThreadLocalRandom.current().nextInt(Integer.parseInt(xr[0]), Integer.parseInt(xr[1]));

            String[] zr = getZRange().split(" ! ");
            int z = ThreadLocalRandom.current().nextInt(Integer.parseInt(zr[0]), Integer.parseInt(zr[1]));

            World world = Bukkit.getWorld("world");
            int y = world.getHighestBlockYAt(x, z);
            if (isLocationSafe(world, x, y, z)) {
                Location location = new Location(world, x, y, z);
                if (!ignorePermissions) {
                    if (cooldown != -1 && !player.hasPermission("rtp.cooldown.bypass")) {
                        if (cooldowns.containsKey(player)) throw new IllegalStateException("COOLDOWN_" + cooldowns.get(player));
                        else {
                            cooldowns.put(player, cooldown);
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    cooldowns.put(player, cooldowns.get(player)-1);
                                    if (cooldowns.get(player) == 0) {
                                        cooldowns.remove(player);
                                        this.cancel();
                                    }
                                }

                            }.runTaskTimer(plugin, 20, 20);
                        }
                    }
                }
                if (getHunger() != null) {
                    int hunger = Integer.parseInt(getHunger());
                    if (player.getFoodLevel() < hunger) {
                        throw new IllegalStateException("NO_HUNGER");
                    }
                    player.setFoodLevel(player.getFoodLevel() - hunger);
                }
                executeActions(player, location, null);
                if (getTeleportType().equals(ChannelManager.TeleportType.NORMAL)) {
                    return new ChannelManager.TeleportLocationData(location, location.getBlockX(), location.getBlockY(), location.getBlockZ(), null);
                } else if (getTeleportType().equals(ChannelManager.TeleportType.SKY)) {
                    plugin.getChannelManager().getTeleportingPlayers().add(player);
                    location.setY(y+100);
                    return new ChannelManager.TeleportLocationData(location, location.getBlockX(), y, location.getBlockZ(), null);
                }
            } else {
                return generateLocation(player, ignorePermissions);
            }
        } else if (getType().equals(ChannelManager.ChannelType.NEARBY_PLAYERS)) {
            String[] nr = getNearbyRange().split(" ! ");
            if (Bukkit.getOnlinePlayers().size() < plugin.getSettings().getNearbyPlayersMinOnline()) {
                throw new IllegalStateException("MIN_ONLINE");
            }
            List<Player> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().getName().equals("world") && !p.getName().equalsIgnoreCase(player.getName())) {
                    players.add(p);
                }
            }
            if (players.size() == 0) {
                throw new IllegalStateException("EMPTY_WORLD");
            }
            Player randomPlayer = players.get(new Random().nextInt(players.size()));
            Location location = randomPlayer.getLocation();

            int distanceX = ThreadLocalRandom.current().nextInt(Integer.parseInt(nr[0]), Integer.parseInt(nr[1]));
            int distanceZ = ThreadLocalRandom.current().nextInt(Integer.parseInt(nr[0]), Integer.parseInt(nr[1]));
            location.setX(location.getX() + distanceX);
            location.setZ(location.getZ() + distanceZ);
            int y = Bukkit.getWorld("world").getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
            location.setY(y);

            if (!ignorePermissions) {
                if (cooldown != -1 && !player.hasPermission("rtp.cooldown.bypass")) {
                    if (cooldowns.containsKey(player)) throw new IllegalStateException("COOLDOWN_" + cooldowns.get(player));
                    else {
                        cooldowns.put(player, cooldown);
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                cooldowns.put(player, cooldowns.get(player)-1);
                                if (cooldowns.get(player) == 0) {
                                    cooldowns.remove(player);
                                    this.cancel();
                                }
                            }

                        }.runTaskTimer(plugin, 20, 20);
                    }
                }
            }

            if (getHunger() != null) {
                int hunger = Integer.parseInt(getHunger());
                if (player.getFoodLevel() < hunger) {
                    throw new IllegalStateException("NO_HUNGER");
                }
                player.setFoodLevel(player.getFoodLevel() - hunger);
            }
            executeActions(player, location, randomPlayer);
            if (getTeleportType().equals(ChannelManager.TeleportType.NORMAL)) {
                return new ChannelManager.TeleportLocationData(location, location.getBlockX(), location.getBlockY(), location.getBlockZ(), randomPlayer);
            } else if (getTeleportType().equals(ChannelManager.TeleportType.SKY)) {
                plugin.getChannelManager().getTeleportingPlayers().add(player);
                location.setY(location.getY()+100);
                return new ChannelManager.TeleportLocationData(location, location.getBlockX(), y, location.getBlockZ(), randomPlayer);
            }

        }

        return null;
    }

    private boolean isLocationSafe (World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        Block blockBelow = world.getBlockAt(x, y - 1, z);
        Block blockAbove = world.getBlockAt(x, y + 1, z);

        return !(blacklistedBlocks.contains(blockBelow.getType().toString().toLowerCase()) || block.getType() != Material.AIR || blockAbove.getType() != Material.AIR);
    }

    private void executeActions(Player player, Location location, Player randomPlayer) {
        if (randomPlayer == null) {
            for (ChannelManager.TeleportAction action : getActions()) {
                switch(action.getName()) {
                    case "effect":
                        for (String eff : action.getArgs()) {
                            String[] effect = eff.split(" ");
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect[0].toUpperCase()), Integer.parseInt(effect[1]), Integer.parseInt(effect[2])-1));
                        }
                        break;
                    case "title":
                        String[] args = action.getArgs();
                        if (args.length == 1) {
                            player.sendTitle(args[0].replace("&", "\u00a7")
                                    .replace("{x}", String.valueOf(location.getBlockX()))
                                    .replace("{y}", String.valueOf(location.getBlockY()))
                                    .replace("{z}", String.valueOf(location.getBlockX())), null, 5, 60, 5);
                        } else if (args.length == 2) {
                            player.sendTitle(
                                    args[0].replace("&", "\u00a7")
                                    .replace("{x}", String.valueOf(location.getBlockX()))
                                    .replace("{y}", String.valueOf(location.getBlockY()))
                                    .replace("{z}", String.valueOf(location.getBlockZ())),
                                    args[1].replace("&", "\u00a7")
                                    .replace("{x}", String.valueOf(location.getBlockX()))
                                    .replace("{y}", String.valueOf(location.getBlockY()))
                                    .replace("{z}", String.valueOf(location.getBlockZ())), 5, 60, 5);
                        }
                        break;
                }
            }
        } else {
            for (ChannelManager.TeleportAction action : getActions()) {
                switch(action.getName()) {
                    case "effect":
                        for (String eff : action.getArgs()) {
                            String[] effect = eff.split(" ");
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect[0].toUpperCase()), Integer.parseInt(effect[1]), Integer.parseInt(effect[2])-1));
                        }
                        break;
                    case "title":
                        String[] args = action.getArgs();
                        if (args.length == 1) {
                            player.sendTitle(
                                    args[0].replace("&", "\u00a7").replace("{player}", randomPlayer.getName()), null, 5, 60, 5);
                        } else if (args.length == 2) {
                            player.sendTitle(
                                    args[0].replace("&", "\u00a7").replace("{player}", randomPlayer.getName()),
                                    args[1].replace("&", "\u00a7").replace("{player}", randomPlayer.getName()), 5, 60, 5);
                        }
                        break;
                }
            }
        }
    }

    public String getConfigName() {
        return configName;
    }

    public String getArgument() {
        return argument;
    }

    public ChannelManager.ChannelType getType() {
        return type;
    }

    public boolean isDefault() {
        return default_;
    }

    public String getXRange() {
        return xRange;
    }

    public String getZRange() {
        return zRange;
    }

    public ChannelManager.TeleportType getTeleportType() {
        return teleportType;
    }

    public List<String> getBlacklistedBlocks() {
        return blacklistedBlocks;
    }

    public String getHunger() {
        return hunger;
    }

    public List<ChannelManager.TeleportAction> getActions() {
        return actions;
    }

    public String getNearbyRange() {
        return nearbyRange;
    }
}

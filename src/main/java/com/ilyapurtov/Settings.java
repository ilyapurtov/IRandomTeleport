package com.ilyapurtov;

import org.bukkit.configuration.file.FileConfiguration;

public class Settings {
    private String rtpCommand;
    private int nearbyPlayersMinOnline;

    public void load(FileConfiguration config) {
        this.rtpCommand = config.getString("rtpCommand");
        this.nearbyPlayersMinOnline = config.getInt("nearbyPlayersMinOnline");
    }

    public String getRtpCommand() {
        return rtpCommand;
    }

    public int getNearbyPlayersMinOnline() {
        return nearbyPlayersMinOnline;
    }
}

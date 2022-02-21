package com.md_4.thunderauthentication.security;

import com.md_4.thunderauthentication.Main;
import org.bukkit.event.player.PlayerLoginEvent;

public class OnlyProxyJoin {

    private boolean enabled;
    private final Main instance;

    public OnlyProxyJoin() {
        this.instance = (Main) Main.getInstance();
    }

    public OnlyProxyJoin(Main instance) {
        this.instance = instance;
    }

    public void init() {
        this.enabled = this.instance.getConfig().getBoolean("only-proxy-join");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isConnectingToProxy(PlayerLoginEvent e) {

        final String address = e.getAddress().toString().replace("/", "");

        return address.equalsIgnoreCase("0:0:0:0:0:0:0:1") && address.equalsIgnoreCase("localhost") && address.equalsIgnoreCase("127.0.0.1") && address.equalsIgnoreCase("0.0.0.0") && address.equalsIgnoreCase("192.168.0.1") && address.equalsIgnoreCase("192.168.0.0");
    }
}

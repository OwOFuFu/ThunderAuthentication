package com.md_4.thunderauthentication.security.events;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.utils.GetConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class JoinEvent implements Listener {

    private final Main plugin;

    public JoinEvent(Main instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {

        Player p = e.getPlayer();

        try {
            String pUuid = Main.getUUIDManager().fetchUUID(p.getName()).toString();
            String fUuid = Main.getUUIDManager().fetchUUID(p.getName()).toString();
            UUID realUUID = Main.getUUIDManager().fetchUUID(p.getName());
            boolean correctProxy = Main.getOnlyProxyJoin().isConnectingToProxy(e);

            if ((!pUuid.equalsIgnoreCase(fUuid)) || (Main.getOnlyProxyJoin().isEnabled() && !correctProxy)) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("kick-message")));

                Bukkit.getServer().getOnlinePlayers().stream().filter(player -> player.hasPermission("thunderauthentication.notify.spoof"))
                        .forEach(player -> {

                            boolean silentBans = Main.getInstance().getConfig().getBoolean("silent-bans");
                            String reason = Main.getInstance().getConfig().getString("ban-reason");

                            TextComponent banButton = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                                    Main.getInstance().getConfig().getString("ban-button")));
                            banButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ban " + realUUID + (silentBans ? " -s " : " ") + reason));
                            if(GetConfig.getPremiumLanguageIT()){
                                banButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&cClicca qui per bannare il giocatore.")).create()));
                            }
                            if(GetConfig.getPremiumLanguageEN()){
                                banButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&cClick here to ban the player.")).create()));
                            }

                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    Main.getInstance().getConfig().getString("staff-alert-message").replace("%player%", p.getName())));
                            player.spigot().sendMessage(banButton);
                        });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}

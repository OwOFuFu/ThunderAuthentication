package com.md_4.thunderauthentication.commands;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.MojangAPI;
import com.md_4.thunderauthentication.backend.core.engine.Language;
import com.md_4.thunderauthentication.backend.core.engine.ThunderAUtils;
import com.md_4.thunderauthentication.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.md_4.thunderauthentication.backend.ThunderAPI.isLogged;
import static com.md_4.thunderauthentication.backend.ThunderAPI.isRegistered;

public class AuthPremium implements CommandExecutor {

    private final Main plugin;
    public AuthPremium(Main instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COMMAND_NO_CONSOLE.toString()));
            return true;
        }

        Player player = (Player) sender;

        if(player.hasPermission("thunderauthentication.premium.authenticate")){
            try {
                if((isRegistered(player))){
                    if(isLogged(player)){
                        if(Main.getInstance().getConfig().getString("saving.mysql_filemode").equals("Backup") || GetConfig.main().getString("saving.mysql_filemode").equals("")){
                            MojangAPI.isPremium(player);
                        } else {
                            if(GetConfig.getPremiumLanguageEN()){
                                player.sendMessage(Format.color("&cThis Function Requires MySQL Connected On File (config.yml) In String > (mysql_filemode) = (Backup) != (Save)"));
                            }
                            if(GetConfig.getPremiumLanguageIT()){
                                player.sendMessage(Format.color("&cQuesta Funzione Richiede MySQL Connesso Al File (config.yml) Alla Stringa > (mysql_filemode) = (Backup) != (Save)"));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if(GetConfig.getPremiumLanguageEN()){
                    player.sendMessage(Format.color("&cThis Function Requires MySQL Connected On File (config.yml) In String > (mysql_filemode) = (Backup) != (Save)"));
                }
                if(GetConfig.getPremiumLanguageIT()){
                    player.sendMessage(Format.color("&cQuesta Funzione Richiede MySQL Connesso Al File (config.yml) Alla Stringa > (mysql_filemode) = (Backup) != (Save)"));
                }
            }
        } else {
            if(GetConfig.getPremiumLanguageIT()){
                player.sendMessage(Format.color( MessagesIT.accessDeniedIT()));
            }
            if(GetConfig.getPremiumLanguageEN()){
                player.sendMessage(Format.color( MessagesEN.accessDeniedEN()));
            }

            player.playSound(player.getLocation(), SoundManager.accessDenied(), 1, 1);
        }
        return false;
    }
}

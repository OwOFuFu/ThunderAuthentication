package com.md_4.thunderauthentication.commands;


import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.MojangAPI;
import com.md_4.thunderauthentication.backend.core.engine.Language;
import com.md_4.thunderauthentication.backend.core.engine.ThunderAUtils;
import com.md_4.thunderauthentication.functions.TableFunctionSP;
import com.md_4.thunderauthentication.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.md_4.thunderauthentication.Main.SQL;

public class UnPremium implements CommandExecutor {

    private final Main plugin;
    public UnPremium(Main instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COMMAND_NO_CONSOLE.toString()));
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("thunderauthentication.premium.deauthenticate")) {

            if (Main.getInstance().getConfig().getString("saving.mysql_filemode").equals("Backup") || GetConfig.main().getString("saving.mysql_filemode").equals("")) {
                Statement st = null;
                try {
                    st = SQL.getConnection().createStatement();
                } catch (SQLException e) {
                    if(GetConfig.getPremiumLanguageEN()){
                        player.sendMessage(Format.color("&cThis Function Requires MySQL Connected On File (config.yml) In String > (mysql_filemode) = (Backup) != (Save)"));
                    }
                    if(GetConfig.getPremiumLanguageIT()){
                        player.sendMessage(Format.color("&cQuesta Funzione Richiede MySQL Connesso Al File (config.yml) Alla Stringa > (mysql_filemode) = (Backup) != (Save)"));
                    }
                }

                if (args.length == 0) {
                    player.sendMessage(Format.color(MessagesIT.usageIT()));
                    player.playSound(player.getLocation(), SoundManager.usage(), 1, 1);
                }

                if (args.length >= 1) {

                    String query = "SELECT * FROM " + args[0];

                    try {

                        ResultSet rs = st.executeQuery(query);

                        while (rs.next()) {

                            String playerName = rs.getString("name");
                            String playerUUID = rs.getString("uuid");
                            String checkPremium = rs.getString("premium");
                            String actualUUIDStr = MojangAPI.getUUID(player.getName()).toString();

                            String sql = "DROP TABLE " + playerName;

                            if (playerName.equals(args[0])) {
                                try {

                                    st.executeUpdate(sql);
                                    TableFunctionSP.createTable(player, player.getUniqueId());
                                    if (GetConfig.getPremiumLanguageIT()) {
                                        player.sendMessage(Format.color(MessagesIT.tableDeletedIT()));
                                    }
                                    if (GetConfig.getPremiumLanguageEN()) {
                                        player.sendMessage(Format.color(MessagesEN.tableDeletedEN()));
                                    }
                                    player.playSound(player.getLocation(), SoundManager.tableDeleted(), 1, 1);
                                    rs.close();


                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        while (!rs.next()) {
                            if (GetConfig.getPremiumLanguageIT()) {
                                player.sendMessage(Format.color(MessagesIT.tableNotFoundIT()));
                            }
                            if (GetConfig.getPremiumLanguageEN()) {
                                player.sendMessage(Format.color(MessagesEN.tableNotFoundEN()));
                            }
                            player.playSound(player.getLocation(), SoundManager.tableNotFound(), 1, 1);
                            rs.close();
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if(GetConfig.getPremiumLanguageEN()){
                    player.sendMessage(Format.color("&cThis Function Requires MySQL Connected On File (config.yml) In String > (mysql_filemode) = (Backup) != (Save)"));
                }
                if(GetConfig.getPremiumLanguageIT()){
                    player.sendMessage(Format.color("&cQuesta Funzione Richiede MySQL Connesso Al File (config.yml) Alla Stringa > (mysql_filemode) = (Backup) != (Save)"));
                }
            }
        } else {

            if (GetConfig.getPremiumLanguageIT()) {
                player.sendMessage(Format.color(MessagesIT.accessDeniedIT()));
            }
            if (GetConfig.getPremiumLanguageEN()) {
                player.sendMessage(Format.color(MessagesEN.accessDeniedEN()));
            }
            player.playSound(player.getLocation(), SoundManager.accessDenied(), 1, 1);
        }
        return false;
    }
}

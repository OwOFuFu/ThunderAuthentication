package com.md_4.thunderauthentication.events;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.MojangAPI;
import com.md_4.thunderauthentication.functions.TableFunctionSP;
import com.md_4.thunderauthentication.utils.GetConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.md_4.thunderauthentication.Main.SQL;

public class AutomaticAuth implements Listener {

    private final Main plugin;

    public AutomaticAuth(Main instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) throws Exception {


        if(GetConfig.main().getString("saving.mysql_filemode").equals("Backup") || GetConfig.main().getString("saving.mysql_filemode").equals("")){
            Statement st = SQL.getConnection().createStatement();
            Statement stmt = SQL.getConnection().createStatement();

            Player player = event.getPlayer();

            DatabaseMetaData meta = SQL.getConnection().getMetaData();
            ResultSet resultSet = meta.getTables(null, null, player.getName(), new String[] {"TABLE"});

            if(!(resultSet.next())){
                TableFunctionSP.createTable(player, MojangAPI.getUUID(player.getName()));
            }

//            if(isRegistered(player)){
//
//                try {
//                    String query = "SELECT * FROM " + player.getName();
//
//                    ResultSet rs = st.executeQuery(query);
//
//                    while (rs.next()) {
//                        String playerName = rs.getString("name");
//                        String playerUUID = rs.getString("uuid");
//                        String checkPremium = rs.getString("premium");
//                        String actualUUIDStr = String.valueOf(MojangAPI.getUUID(player.getName()));
//
//                        if((playerName.equals(player.getName()))){
//                            if(!(playerUUID.equals(actualUUIDStr))){
//                                if(GetConfig.getPremiumLanguageEN()){
//                                    player.kickPlayer(Format.color(MessagesEN.spoofedPlayerEN())); // Hacker
//                                }
//                                if(GetConfig.getPremiumLanguageIT()){
//                                    player.kickPlayer(Format.color(MessagesIT.spoofedPlayerIT())); // Hacker
//                                }
//                            } else {
//                                if((playerUUID.contains(actualUUIDStr))) {
//                                    if (checkPremium.equals("SI")) {
//                                        setLogged(player); // Login
//                                        rs.close();
//                                        player.playSound(player.getLocation(), SoundManager.logged(), 1, 1);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}

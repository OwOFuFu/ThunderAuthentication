package com.md_4.thunderauthentication.functions;

import com.md_4.thunderauthentication.backend.core.MojangAPI;
import com.md_4.thunderauthentication.utils.Format;
import com.md_4.thunderauthentication.utils.GetConfig;
import com.md_4.thunderauthentication.utils.MessagesEN;
import com.md_4.thunderauthentication.utils.MessagesIT;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static com.md_4.thunderauthentication.Main.SQL;


public class UpdateUUID {

    public static void update(Player player, UUID uuid) throws Exception {

        Statement st = SQL.getConnection().createStatement();

        String query = "SELECT * FROM " + player.getName();

        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            String playerName = rs.getString("name");
            String playerUUID = rs.getString("uuid");
            String playerOFFUUID = rs.getString("offlineuuid");
            String checkPremium = rs.getString("premium");
            String actualUUIDStr = String.valueOf(MojangAPI.getUUID(player.getName()));
            String dropDeprecated = "DROP TABLE " + player.getName();

            if ((playerName.equals(player.getName()))) {
                if (checkPremium.equals("SI")) {
                    if(MojangAPI.getUUID(MojangAPI.getNAME(player.getName())).equals(MojangAPI.getUUID(playerName))){
                        st.executeUpdate(dropDeprecated);
                        TableFunction.createTable(player, MojangAPI.getUUID(player.getName()));
                        if(GetConfig.getPremiumLanguageIT()){
                            player.kickPlayer(Format.color( MessagesIT.updatedUUIDIT()));
                        }
                        if(GetConfig.getPremiumLanguageEN()){
                            player.kickPlayer(Format.color( MessagesEN.updatedUUIDEN()));
                        }
                    }
                }
            }
        }
    }
}

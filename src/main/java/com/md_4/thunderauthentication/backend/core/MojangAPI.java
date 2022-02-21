package com.md_4.thunderauthentication.backend.core;


import com.md_4.thunderauthentication.functions.TableFunction;
import com.md_4.thunderauthentication.utils.*;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static com.md_4.thunderauthentication.Main.SQL;

public class MojangAPI {

    public static boolean isPremium(Player player) {

        String name = player.getName();
        UUID actualUUID = player.getUniqueId();
        String actualUUIDStr = player.getUniqueId().toString();
        String offlineUUIDStr = getMd5("OfflinePlayer:"+name);

        if(offlineUUIDStr.equals(actualUUIDStr)) {
            isNotPermiumPlayerMessage(player);
            return true;
        }
        setUsernamePremium(player.getName(), player);
        return false;

    }

    public static String getMd5(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean setUsernamePremium(String username, Player player) {

        String query = "SELECT * FROM " + player.getName();

        try {
            Statement st = SQL.getConnection().createStatement();

            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                String playerName = rs.getString("name");
                String playerUUID = rs.getString("uuid");
                String checkPremium = rs.getString("premium");
                String actualUUIDStr = MojangAPI.getUUID(player.getName()).toString();
                String sql = "DROP TABLE " + player.getName();


                if(playerName.equals(player.getName())) {
                    if (playerUUID.equals(actualUUIDStr)) {
                        if(checkPremium.equals("NO")){
                                st.executeUpdate(sql);
                                TableFunction.createTable(player, MojangAPI.getUUID(player.getName()));
                                if(GetConfig.getPremiumLanguageIT()){
                                    player.sendMessage(Format.color( MessagesIT.addedToPremiumListIT()));
                                }
                                if(GetConfig.getPremiumLanguageEN()){
                                    player.sendMessage(Format.color( MessagesEN.addedToPremiumListEN()));
                                }
                                player.playSound(player.getLocation(), SoundManager.premiumADDED(), 1, 1);
                        } else {
                            if(GetConfig.getPremiumLanguageIT()){
                                player.sendMessage(Format.color( MessagesIT.alreadyAddedToPremiumListIT()));
                            }
                            if(GetConfig.getPremiumLanguageEN()){
                                player.sendMessage(Format.color( MessagesEN.alreadyAddedToPremiumListEN()));
                            }
                            player.playSound(player.getLocation(), SoundManager.alreadyAddedToPremiumList(), 1, 1);
                        }
                    }
                }

                if((playerName.equals(player.getName()))){
                    if(!(playerUUID.equals(actualUUIDStr))){
                            player.kickPlayer(Format.color(MessagesIT.spoofedPlayerIT()));
                        } else {
                            if((playerUUID.contains(actualUUIDStr))) {
                                if(checkPremium.equals("SI")){
                                    if(GetConfig.getPremiumLanguageIT()){
                                        player.sendMessage(Format.color( MessagesIT.alreadyAddedToPremiumListIT()));
                                    }
                                    if(GetConfig.getPremiumLanguageEN()){
                                        player.sendMessage(Format.color( MessagesEN.alreadyAddedToPremiumListEN()));
                                    }
                                    player.playSound(player.getLocation(), SoundManager.alreadyAddedToPremiumList(), 1, 1);
                                }
                            }
                        }
                    }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    private static boolean isNotPermiumPlayerMessage (Player player){
        player.sendMessage(Format.color( MessagesIT.notPremiumIT()));
        return false;
    }

    public static UUID getUUID(String name) throws Exception {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);

        String uuid = (String)((JSONObject)(new JSONParser()).parse(new InputStreamReader(url.openStream()))).get("id");
        String realUUID = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
        return UUID.fromString(realUUID);
    }

    public static String getNAME(String name) throws Exception {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);

        String getname = (String)((JSONObject)(new JSONParser()).parse(new InputStreamReader(url.openStream()))).get("name");
        return getname;
    }

}

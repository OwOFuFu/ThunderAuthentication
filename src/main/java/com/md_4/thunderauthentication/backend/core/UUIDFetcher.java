package com.md_4.thunderauthentication.backend.core;


import lombok.SneakyThrows;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class UUIDFetcher {

    public static final String uuidURL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    public static final String nameURL = "https://api.mojang.com/users/profiles/minecraft/";

    @SneakyThrows
    public static JSONObject getPlayerJson(UUID uuid, String name) {
        URL url;

        if (name != null) {
            url = new URL(nameURL + name);
        } else {
            url = new URL(uuidURL + uuid);
        }
        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream()));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        if(!stringBuilder.toString().startsWith("{")){
            return null;
        }
        return new JSONObject(stringBuilder.toString());
    }

    public static UUID getOFFUUID(String name){
        String uuidTemp = (String) getPlayerJson(null, name).get("id");
        if(uuidTemp == null){
            return null;
        }
        String uuid = "";
        for (int i = 0; i <= 31; i++) {
            uuid = uuid + uuidTemp.charAt(i);
            if (i == 7 || i == 11 || i == 15 || i == 19) {
                uuid = uuid + "-";
            }
        }

        if(getPlayerJson(null, name) != null){
            return UUID.fromString(uuid);
        }
        return null;
    }

    public static String getName(UUID uuid){
        if(getPlayerJson(uuid, null) != null){
            return (String) getPlayerJson(uuid, null).get("name");
        }
        return null;
    }

}

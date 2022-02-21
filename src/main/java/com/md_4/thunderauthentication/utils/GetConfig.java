package com.md_4.thunderauthentication.utils;

import com.md_4.thunderauthentication.Main;
import org.bukkit.configuration.file.FileConfiguration;

@SuppressWarnings("all")
public class GetConfig {

    public static FileConfiguration main(){
        return Main.getInstance().getConfig();
    }

    public static boolean getPremiumLanguageIT(){
        if(Main.getInstance().getConfig().getString("general.language_file").equals("it_it.properties")){
            return true;
        }
        return false;
    }

    public static boolean getPremiumLanguageEN(){
        if(Main.getInstance().getConfig().getString("general.language_file").equals("en_us.properties")){
            return true;
        }
        return false;
    }
}


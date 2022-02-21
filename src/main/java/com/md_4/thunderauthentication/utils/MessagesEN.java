package com.md_4.thunderauthentication.utils;

import com.md_4.thunderauthentication.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@SuppressWarnings("all")
public class MessagesEN {
    public static File lang_en = new File(Main.getInstance().getDataFolder() + "/premium/en_EN.yml");
    public static FileConfiguration lang_en_config;
    public static String lang;

    public static String notPremiumEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.NotPremium")) : "";
    }

    public static String spoofedPlayerEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.SpoofedPlayer")) : "";
    }

    public static String errorEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.Error")) : "";
    }

    public static String addedToPremiumListEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.PremiumAdded")) : "";
    }

    public static String alreadyAddedToPremiumListEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.AlreadyPremium")) : "";
    }

    public static String tableNotFoundEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.TableNotFound")) : "";
    }

    public static String tableDeletedEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.TableDeleted")) : "";
    }

    public static String accessDeniedEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.AccessDenied")) : "";
    }

    public static String premiumDisabledEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.PremiumDisabled")) : "";
    }

    public static String updatedUUIDEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.UpdatedUUID")) : "";
    }

    public static String usageEN(){
        return lang.equals("en_us.properties") ? Format.color(lang_en_config.getString("Messages.Usage")) : "";
    }

    static {
        lang_en_config = YamlConfiguration.loadConfiguration(lang_en);
        lang = Main.getInstance().getConfig().getString("general.language_file");
    }
}

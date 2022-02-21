package com.md_4.thunderauthentication.utils;

import com.md_4.thunderauthentication.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@SuppressWarnings("all")
public class MessagesIT {
    public static File lang_it = new File(Main.getInstance().getDataFolder() + "/premium/it_IT.yml");
    public static FileConfiguration lang_it_config;
    public static String lang;

    public static String notPremiumIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.NotPremium")) : "";
    }

    public static String spoofedPlayerIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.SpoofedPlayer")) : "";
    }

    public static String errorIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.Error")) : "";
    }

    public static String addedToPremiumListIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.PremiumAdded")) : "";
    }

    public static String alreadyAddedToPremiumListIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.AlreadyPremium")) : "";
    }

    public static String tableNotFoundIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.TableNotFound")) : "";
    }

    public static String tableDeletedIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.TableDeleted")) : "";
    }

    public static String accessDeniedIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.AccessDenied")) : "";
    }

    public static String premiumDisabledIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.PremiumDisabled")) : "";
    }

    public static String updatedUUIDIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.UpdatedUUID")) : "";
    }

    public static String usageIT(){
        return lang.equals("it_it.properties") ? Format.color(lang_it_config.getString("Messages.Usage")) : "";
    }

    static {
        lang_it_config = YamlConfiguration.loadConfiguration(lang_it);
        lang = Main.getInstance().getConfig().getString("general.language_file");
    }
}

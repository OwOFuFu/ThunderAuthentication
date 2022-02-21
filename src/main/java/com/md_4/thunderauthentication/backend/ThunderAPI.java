package com.md_4.thunderauthentication.backend;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.engine.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ThunderAPI {


    public static boolean isRegistered(Player player){
        AuthPlayer ap = AuthPlayer.getAuthPlayer(player);
        return ap.isRegistered();
    }

    public static boolean isLogged(Player player){
        AuthPlayer ap = AuthPlayer.getAuthPlayer(player);
        return ap.isLoggedIn();
    }

    public static void setLogged(Player p){
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        ap.login();

        if (Config.useHideInventory) ap.RestoreSurvivalInventory(p);

        Main.getInstance().getLogger().info(p.getName() + " " + ThunderAUtils.colorize(Language.HAS_LOGGED_IN.toString()));
        p.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.LOGGED_IN_SUCCESSFULLY.toString()));
        if(!ap.getCurrentIPAddress().equals(ap.getLastIPAddress())) {
            // Spawn off a asynchronously process to Update Ip-Address-Count the player is coming from
            new BukkitRunnable() {
                @Override
                public void run() {
                    PConfManager.removePlayerFromIp(ap.getLastIPAddress()); // "192.168.1.7"
                    PConfManager.addPlayerToIp(ap.getCurrentIPAddress()); // "192.168.1.7"
                }
            }.runTaskAsynchronously(Main.getInstance());
        }
        // NEW !!  ASK THE PLAYER to set the EMAIL-Address !!!!
        // The '#' means, the player was unable to Confirm the Email-Address (could it be wrong?)
        if((ap.getEmailAddress().equals("") | ap.getEmailAddress().contains("#")) & Config.emailForceSet) {
            ap.createSetEmailReminder(Main.getInstance());
        }

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        for (String playerAction : Config.playerActionLogin) {
            if(!playerAction.trim().isEmpty()) {
                //Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));

                try {
                    if(playerAction.contains("TWait("))
                        ThunderAUtils.createRunLaterCommand(ap.getUserName(), playerAction);
                    else
                        Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));
                } catch (Exception  error  ) {
                    Main.getInstance().getLogger().info("Error OnLogin Executing: " + playerAction.replace("$P", ap.getUserName()) );
                    error.printStackTrace();
                }

                //ThunderAuthentication.MyQueue.Put("executeConsoleCommand:~" + playerAction.replace("$P", ap.getUserName()));
            }
        }
    }

}

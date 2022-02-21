package com.md_4.thunderauthentication.backend.core.engine.commands;

import com.md_4.thunderauthentication.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.md_4.thunderauthentication.backend.core.engine.AuthPlayer;
import com.md_4.thunderauthentication.backend.core.engine.Config;
import com.md_4.thunderauthentication.backend.core.engine.Language;
import com.md_4.thunderauthentication.backend.core.engine.ThunderAUtils;
import org.jetbrains.annotations.NotNull;

public class CmdLogout implements CommandExecutor {

    @SuppressWarnings("unused") // Despite "unused": IT IS NEEDED in then onEnable Event !!!!!
    private final Main plugin;

    public CmdLogout(Main instance) {
        this.plugin = instance;
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args)
    {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COMMAND_NO_CONSOLE.toString()));
            return true;
        }

        return LogMeOff((Player) cs, cmd.getName());
  	}


    public static boolean CmdLogMeOff(CommandSender cs, String cmd)
    {
  		return LogMeOff((Player) cs, cmd);
    }
    
    private static boolean LogMeOff(Player cs, String cmd) {
        if (cmd.equalsIgnoreCase("logout")) {
            if (!cs.hasPermission("thunderauthentication.logout")) {
                ThunderAUtils.dispNoPerms(cs);
                return true;
            }

            Player p = (Player) cs;
            AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
            if (!ap.isLoggedIn()) {
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.NOT_LOGGED_IN.toString()));
                return true;
            }
            cs.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.LOGGED_OUT.toString()));
            ap.setLastQuitTimestamp(System.currentTimeMillis());
            ap.setLastJoinTimestamp(System.currentTimeMillis());
            ap.logout(Main.getInstance(), true);

            if (Config.useHideInventory) ap.HideSurvivalInventory(p);
            
	        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            for (String playerAction : Config.playerActionLogof) {
    			if(!playerAction.trim().isEmpty()) {
    	            //Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));

    	        	try {
    	        		if(playerAction.contains("TWait("))
    	        			ThunderAUtils.createRunLaterCommand(ap.getUserName(), playerAction);
    	        		else
    	        			Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));
    	        	} catch (Exception  error  ) {
    	        		Main.getInstance().getLogger().info("Error OnLogof Executing: " + playerAction.replace("$P", ap.getUserName()) );
    	            	error.printStackTrace();
    	        	}

    			}
    		}
            
            return true;
        }
        return false;
    }

}

package com.md_4.thunderauthentication.backend.core.engine.commands;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.engine.tools.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.md_4.thunderauthentication.backend.core.engine.AuthPlayer;
import com.md_4.thunderauthentication.backend.core.engine.Config;
import com.md_4.thunderauthentication.backend.core.engine.Hasher;
import com.md_4.thunderauthentication.backend.core.engine.Language;
import com.md_4.thunderauthentication.backend.core.engine.PConfManager;
import com.md_4.thunderauthentication.backend.core.engine.ThunderAUtils;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CmdLogin implements CommandExecutor {

    @SuppressWarnings("unused") // Despite "unused": IT IS NEEDED in then onEnable Event !!!!!
	private final Main plugin;

    public CmdLogin(Main instance) {
        this.plugin = instance;
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args)
    {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COMMAND_NO_CONSOLE.toString()));
            return true;
        }

        String rawPassword="";
        if (args.length < 1) { // No Password given
    		if(Config.UseAutoMenu) { // Use inventory menu?
    			rawPassword="menu";
    		}
    		else
    		{
    			//cs.sendMessage(cmd.getDescription());
    			cs.sendMessage(String.format(Language.USAGE_LOGIN2.toString(), new Object[] { cmd }));
    			cs.sendMessage(Language.USAGE_LOGIN1.toString());
    			
    			return false;
    		}
        }
        else rawPassword = ThunderAUtils.getFinalArg(args, 0).trim(); // support spaces

  		return LogMeIn((Player) cs, cmd.getName(), rawPassword);
  	}


    public static boolean CmdLogMeIn(CommandSender cs, String cmd, String rawPassword)
    {    	
    	if (rawPassword.trim()=="") {  // No Password given
    		if(Config.UseAutoMenu) { // Use inventory menu?
    			rawPassword="menu";
    		}
    		else
    			{
    			cs.sendMessage(String.format(Language.USAGE_LOGIN0.toString(), new Object[] { cmd }));
    			cs.sendMessage(Language.USAGE_LOGIN1.toString());

    			return false;
    		}
    	}

  		return LogMeIn((Player) cs, cmd, rawPassword.trim());
    }
    
    private static boolean LogMeIn(Player cs, String cmd, String rawPassword) {
    	if (cmd.equalsIgnoreCase("login")) {
            if (!cs.hasPermission("thunderauthentication.login")) {
                ThunderAUtils.dispNoPerms(cs);
                return true;
            }

            Player p = (Player) cs;
            final AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
            if (ap.isLoggedIn()) {
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.ALREADY_LOGGED_IN.toString()));
                return true;
            }

            // TEST MET INVENTORY PASSWORD CHECKER, zie: https://www.youtube.com/watch?v=XenOtWM597Q
            if(rawPassword.equals("menu")) {
       			String PsswrdMessage = ThunderAUtils.colorize(Language.PASSWORD_MESSAGE.toString());
            	cs.openInventory(ThunderAUtils.GetMenuInventory("Password", PsswrdMessage, null));
            	
            	//Inventory inv = Bukkit.createInventory(null, 9, PsswrdMessage); // maak inventory met 9 slots en "title"
            	//inv.addItem(new ItemStack(Material.SAND,1)); // plaats 1 SAND op de eerst beschikbare plaats/positie
            	//inv.addItem(new ItemStack(Material.DIRT,1)); // plaats 1 DIRT op de eerst beschikbare plaats/positie
            	//inv.setItem(5, new ItemStack(Material.GRAVEL,14)); // plaats een Stack van 14 DIAMOND op positie 5.            	
            	//cs.openInventory(inv);
            	return true;
            }

            //String rawPassword = ThunderAuthentication.getFinalArg(args, 0); // support spaces
            for (String disallowed : Config.disallowedPasswords) {
                if (disallowed.equals("#NoPlayerName#")) disallowed = ap.getUserName();
                if (!rawPassword.equalsIgnoreCase(disallowed)) continue;
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.DISALLOWED_PASSWORD.toString()));
            }
            String hashedPassword="";
            String hashedMenuPassword="";
            String hashType = (!ap.getHashType().equalsIgnoreCase(Config.passwordHashType)) ? ap.getHashType() : Config.passwordHashType;
            try { // Set Both password-types: normal and pictogram based. pictogram uses username to differentiate.
                hashedPassword = Hasher.encrypt(rawPassword, hashType); // Hashed password version of Player input RawPassword.
                hashedMenuPassword = Hasher.encrypt(ap.getUserName() + rawPassword, hashType); // Same as hashedPassword but includes username.
            } catch (NoSuchAlgorithmException e) {
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COULD_NOT_LOG_IN.toString()));
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.ADMIN_SET_UP_INCORRECTLY.toString()));
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.CONTACT_ADMIN.toString()));
                return true;
            }

			//ap.setPwdOldStatus(""); // Clear status for InventoryClick-Event Dit zal niet de ChangePass fout zijn (tijdelijk neergezet)...
			//ap.setPwdNewStatus(""); // Clear status for InventoryClick-Event Dit zal niet de ChangePass fout zijn (tijdelijk neergezet)...
            
            String SavedPassword = ap.getPasswordHash(); // Old/Saved Hashed password of player. 

            if (!Config.MySqlDbHost.equals("")) {
                if (!hashedPassword.equals(SavedPassword) && !hashedMenuPassword.equals(SavedPassword)) {
                	// Do the SqlStatements, Wrong password, check if player has changes password on other server  
                	try {
                		// Connection con = MySQL.getConnection(); 
                		PreparedStatement ps = MySQL.getConnection().prepareStatement(
                						"SELECT Password, Hash " +
                						"FROM   Players " +
    		        					"WHERE  Name  = ? ");
                		ps.setString(1, p.getName());
                		ResultSet res = ps.executeQuery();
                		//Code using ResultSet entries here
                		if (res.next() == true) { 
                			// We have a record, Tell ThunderAuthentication THIS is the correct password (no Hashing!!, it is Hashed!!).
                			ap.setHashedPassword(res.getString("Password"), res.getString("Hash"));
                			SavedPassword=ap.getPasswordHash();
                		}
                		res.close();
                		ps.close();
    	        	} catch (SQLException e) {
   	        			// TODO Auto-generated catch block
   	        			e.printStackTrace();
    	        	}
                }
            }

            if (hashedPassword.equals(SavedPassword) | hashedMenuPassword.equals(SavedPassword)) {
                ap.login();
                
                if (Config.useHideInventory) ap.RestoreSurvivalInventory(p);

                Main.getInstance().getLogger().info(p.getName() + " " + ThunderAUtils.colorize(Language.HAS_LOGGED_IN.toString()));
                cs.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.LOGGED_IN_SUCCESSFULLY.toString()));
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
            } else {
            	Main.getInstance().getLogger().warning(p.getName() + " " + ThunderAUtils.colorize(Language.USED_INCORRECT_PASSWORD.toString()));
                if(Config.KickOnPasswordFail) {
                	//p.kickPlayer(Language.INCORRECT_PASSWORD.toString());
                	//return false;
                	final Player pl=p;
                	Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> pl.kickPlayer(ThunderAUtils.colorize(Language.INCORRECT_PASSWORD.toString())), 10L);

                }
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.INCORRECT_PASSWORD.toString()));
            }
            return true;
        }
        return false;
    }

}

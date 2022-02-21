package com.md_4.thunderauthentication.backend.core.engine.commands;

import java.util.logging.Logger;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.engine.tools.SMTP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.md_4.thunderauthentication.backend.core.engine.ThunderAUtils;
import com.md_4.thunderauthentication.backend.core.engine.AuthPlayer;
import com.md_4.thunderauthentication.backend.core.engine.Config;
import com.md_4.thunderauthentication.backend.core.engine.Language;
import com.md_4.thunderauthentication.backend.core.engine.PConfManager;
import org.jetbrains.annotations.NotNull;

public class CmdSetEmail implements CommandExecutor {

    @SuppressWarnings("unused") // Despite "unused": IT IS NEEDED in then onEnable Event !!!!!
	private final Main plugin;

    public CmdSetEmail(Main instance) {
        this.plugin = instance;
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args)
    {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COMMAND_NO_CONSOLE.toString()));
            return true;
        }

        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
          	//cs.sendMessage(String.format(ThunderAuthentication.colorize(Language.USAGE_LOGIN2.toString()),cmd));
            return false;
        }

  		String EmailAddress = ThunderAUtils.getFinalArg(args, 0).trim(); // support spaces
  		return SetMyMail((Player) cs, cmd.getName(), EmailAddress);
  	}


    public static boolean CmdSetMyMail(CommandSender cs, String cmd, String EmailAddress)
    {
  		return SetMyMail((Player) cs, cmd, EmailAddress.trim());
    }
    
    private static boolean SetMyMail(Player cs, String cmd, String EmailAddress) {
        if (cmd.equalsIgnoreCase("setemail")) {        	
        	int EmlCode = 0;

            final Player p = (Player) cs;
            final AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
            if (!ap.isLoggedIn()) {
                cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.NOT_LOGGED_IN.toString()));
                return true;
            }

            //if (!cs.hasPermission("tauth.setemail")) {
            //    ThunderAuthentication.dispNoPerms(cs);
            //    return true;
            //}
            if (EmailAddress == "") {
            	if(ap.getEmailAddress().contains("#")) {
            		cs.sendMessage(ThunderAUtils.colorize(Language.EMAIL_ADDRESS.toString()) + " " + ap.getEmailAddress().replace("#", "") + " " + ThunderAUtils.colorize(Language.NOTCONFIRMD.toString()));
            	} else {
            		cs.sendMessage(ThunderAUtils.colorize(Language.CURRENT.toString() + " " + Language.EMAIL_ADDRESS.toString()) + ": " + ap.getEmailAddress().replace("#", ""));
            	}
                return false;
            }

        	//String EmailAddress = args[0]; // No Space Support

        	if (EmailAddress.contains("-confirm")) {
        		// SetEmailAddress Confirmation, player has received confirmation email.
        		// if Confirmation is correct then kill the SetEmail reminder.
        		// At this time Player still has unusable EmailAddress starting with '#' 
        		final String CurrEml = ap.getEmailAddress().replace("#", "");

        		// calculate  Email address Integer-Value
        		for(int i=1 ; i<CurrEml.length() && i<15 ; i++){
            		EmlCode = EmlCode + CurrEml.codePointAt(i);
            	}
            	if(EmailAddress.contentEquals(Integer.toString(EmlCode) + "-confirm")) {
            		ap.setEmailAddress(CurrEml); // Confirmation correct, Set-EmailAddress 
            		
        			BukkitTask reminder = ap.getCurrentReminderTask();
        			if (reminder != null) reminder.cancel(); // kill the SetEmail reminder
    				cs.sendMessage(ChatColor.RED + String.format(ThunderAUtils.colorize(Language.REGISTERED_SUCCESSFULLY.toString()),CurrEml));
        			
        			// Add the Players email address to the EmailAddress list
    				PConfManager.addPlayerToEm(CurrEml); // "player@nowhere.com"
        			//new BukkitRunnable() {
        			//	@Override
        			//	public void run() {        		    	
        			//		PConfManager.addPlayerToEm(CurrEml); // "player@nowhere.com"
        			//	}
        			//}.runTaskAsynchronously(ThunderAuthentication.getInstance());
            	}            	
        		
        		// if NOT correct, Clear out then current Email-Address?? (sets next login reminder)
        		
            	// EmailAddress contains "-confirm", meaning ONLY Confirmation-Checking, so Return!!!
        		return true;
        	}
        	
        	// No "-confirm emailaddress", player is setting his Email-Address, Check Address,
        	// Save it in the player Profile and send Confirmation-email to check trust-worthy

			for (String disallowed : Config.disallowedEmlAdresses) {
				if (!EmailAddress.toLowerCase().contains(disallowed.toLowerCase())) continue;
				cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.DISALLOWED_EMLADDRESS.toString()));
				return true;
			}

			if (!EmailAddress.contains("@")) {
                cs.sendMessage(ChatColor.RED + String.format(ThunderAUtils.colorize(Language.PLAYER_INVALID_EMAILADDRESS.toString() + "."), p.getName(), EmailAddress));
                return true;
            }

			// reduce the Players email address from the EmailAddress list
			// If The EmailAddress is not found, nothing is reduced.
			PConfManager.removePlayerFromEmSet(ap.getEmailAddress());
			//new BukkitRunnable() {
			//	@Override
			//	public void run() {        		    	
			//		PConfManager.removePlayerFromEmSet(ap.getEmailAddress());
			//	}
			//}.runTaskAsynchronously(ThunderAuthentication.getInstance());
			
    		ap.setEmailAddress("#" + EmailAddress); 

    		// ----------------------------------------------------------------------------------------------
        	//new BukkitRunnable() {
        	new Thread(() -> {

				int EmlCode1 = 0;
				String Player = ap.getUserName();
				Logger log = Main.getInstance().getLogger();
				String EmailAddress1 = ap.getEmailAddress().replace("#", "");

				for(int i = 1; i< EmailAddress1.length() && i<15 ; i++){
					EmlCode1 = EmlCode1 + EmailAddress1.codePointAt(i);
				}

				int PlayerCount = PConfManager.countPlayersFromEm(EmailAddress1); // "email@address.org","Userid"

				if(Config.maxUsersPerEmaddress>0) {
					log.info("Login Email-Address " + EmailAddress1 + " used by " + PlayerCount + " player(s) ");
					   log.info("Configured maximum allowed players from one Email-Address is: " + Config.maxUsersPerEmaddress);
					   if (PlayerCount >= Config.maxUsersPerEmaddress) {
						   cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.PLAYER_EXCEEDS_MAXREGS_EM.toString()));
						   return; // NOT !!! sending New Password to Email Address !!!
					   }
				   }

				try {
					// SetUp Email Session
					SMTP.Email email = SMTP.createEmptyEmail();
					//email.add("Content-Type", "text/html"); //Headers for the email (useful for html) you do not have to set them
					//email.add("Content-Type", "text/plain");  //Default Header ("text/plain") for the email.you do not have to set
					email.from(Config.emlFromNicer, Config.emlFromEmail); //The sender of the email.
					email.to(Player, EmailAddress1); //The recipient of the email.
					email.subject(Config.emailsubject); //Subject of the email
					email.body(String.format(Config.confirmbodytxt, "/setemail " + Integer.toString(EmlCode1) + "-confirm"));
					// All the email stuff here
					//SMTP.sendEmail(smtpServer, email, password, mail, debug);
					SMTP.sendEmail(Config.emlSmtpServr,
									Config.emlLoginName,
									Config.emlLoginPswd,
									email, false);

					cs.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.EMAIL_SET_AND_CONFIRMSEND.toString()));

				} catch (Exception  error  ) {
					error.printStackTrace();
					cs.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.ADMIN_SET_UP_INCORRECTLY.toString()));
					cs.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.CONTACT_ADMIN.toString()));
				}

			}).start();
			// ----------------------------------------------------------------------------------------------
			
            // NEW !!  ASK THE PLAYER to set the EMAIL-Address !!!!
			BukkitTask reminder = ap.getCurrentReminderTask();
			if (reminder != null) reminder.cancel();
			ap.createSetEmailReminder(Main.getInstance());
			// This reminder-task is killed when player enters the correct confirmation code

    		return true;
        }
        return false;
    }
}

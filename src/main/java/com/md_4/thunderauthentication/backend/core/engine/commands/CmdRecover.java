package com.md_4.thunderauthentication.backend.core.engine.commands;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.engine.tools.SMTP;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.md_4.thunderauthentication.backend.core.engine.ThunderAUtils;
import com.md_4.thunderauthentication.backend.core.engine.AuthPlayer;
import com.md_4.thunderauthentication.backend.core.engine.Config;
import com.md_4.thunderauthentication.backend.core.engine.Language;
import org.jetbrains.annotations.NotNull;


public class CmdRecover implements CommandExecutor {

    @SuppressWarnings("unused") // Despite "unused": IT IS NEEDED in then onEnable Event !!!!!
	private final Main plugin;

    public CmdRecover(Main instance) {
        this.plugin = instance;
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String label, String[] args)
    {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.COMMAND_NO_CONSOLE.toString()));
            return true;
        }

  		return SendNewPwd((Player) cs, cmd.getName());
  	}


    public static boolean CmdSendNewPwd(CommandSender cs, String cmd)
    	{
  		return SendNewPwd((Player) cs, cmd);
    }
    
    private static boolean SendNewPwd(Player cs, String cmd) {
        if (cmd.equalsIgnoreCase("recoverpwd")) {        	

            final Player p = (Player) cs;
            final AuthPlayer ap = AuthPlayer.getAuthPlayer(p);

            if (!Config.emlFromEmail.contains("@")) {
                cs.sendMessage(ChatColor.RED + "Incorrect Config Email Setup.");
                return true;
            }
            if (!ap.getEmailAddress().contains("@")) {
                cs.sendMessage(ChatColor.RED + String.format(ThunderAUtils.colorize(Language.PLAYER_INVALID_EMAILADDRESS.toString() + "."), ap.getUserName(), "'" + ap.getEmailAddress() + "'"));
                return true;
            }

       		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
       		final String Password = RandomStringUtils.random( 7, characters );            	

			if (ap.setPassword(Password, ap.getHashType())) {
				new Thread(() -> {
					String Player = ap.getUserName();

					// SetUp Email Session
					SMTP.Email email = SMTP.createEmptyEmail();
					//email.add("Content-Type", "text/html"); //Headers for the email (useful for html) you do not have to set them
					//email.add("Content-Type", "text/plain");  //Default Header ("text/plain") for the email.you do not have to set
					email.from(Config.emlFromNicer, Config.emlFromEmail); //The sender of the email.
					email.to(Player, ap.getEmailAddress()); //The recipient of the email.
					email.subject(Config.recoversubject); //Subject of the email
					email.body(String.format(Config.recoverbodytxt, Player, Password));
					// All the email stuff here
					//SMTP.sendEmail(smtpServer, email, password, mail, debug);
					SMTP.sendEmail(Config.emlSmtpServr,
									Config.emlLoginName,
									Config.emlLoginPswd,
									email, false);
				}).start();

				cs.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.PASSWORD_RECOVER_MAIL.toString()));
				Main.getInstance().getLogger().info(p.getName() + " !!!! Recover Password send to Player");
				return true;
			}
			else 
				{ 
				cs.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.PASSWORD_COULD_NOT_BE_SET.toString()));
				Main.getInstance().getLogger().info(p.getName() + " !!!! Recover Password could not be set");
			}
        }
        return false;
    }
}

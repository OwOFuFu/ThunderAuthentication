package com.md_4.thunderauthentication.backend.core.engine;

import com.md_4.thunderauthentication.Main;
import com.md_4.thunderauthentication.backend.core.MojangAPI;
import com.md_4.thunderauthentication.backend.core.engine.commands.*;
import com.md_4.thunderauthentication.utils.Format;
import com.md_4.thunderauthentication.utils.GetConfig;
import com.md_4.thunderauthentication.utils.MessagesEN;
import com.md_4.thunderauthentication.utils.MessagesIT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.md_4.thunderauthentication.Main.SQL;

public class AuthListener implements Listener {

    private final Main plugin;

    public AuthListener(Main instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onInvOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;

        // TEST MET INVENTORY PASSWORD CHECKER, zie: https://www.youtube.com/watch?v=XenOtWM597Q
        
    	String CapchaMessage = e.getView().getTitle();
    	String[] CpchaMsg = CapchaMessage.split("'");
    	
    	if(CpchaMsg.length==3) {

    		String OrgCapchaMessage = String.format(ThunderAUtils.colorize(Language.CAPCHA_MESSAGE.toString()),"'--'");


    		if(OrgCapchaMessage.contains(CpchaMsg[0]) & OrgCapchaMessage.contains(CpchaMsg[2])) return;
        }
    	if(CapchaMessage.equals(ThunderAUtils.colorize(Language.PASSWORD_MESSAGE.toString()))) {
            e.setCancelled(false);
    		return;
    	}
    	if(CapchaMessage.equals(ThunderAUtils.colorize(Language.REGISTER_MESSAGE.toString()))) {
            e.setCancelled(false);
    		return;
    	}

        e.setCancelled(true);
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent  e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;

        // TEST MET INVENTORY PASSWORD CHECKER, zie: https://www.youtube.com/watch?v=XenOtWM597Q

    	String CapchaMessage = e.getView().getTitle();
    	String[] CpchaMsg = CapchaMessage.split("'");
    	if(CpchaMsg.length==3) { // We have an Inventory Title containing Single Quotes
    		// Inventory-Title CpchaMsg[0]: first part, CpchaMsg[1]: Capcha Block, CpchaMsg[2]: last part
    		String OrgCapchaMessage = String.format(ThunderAUtils.colorize(Language.CAPCHA_MESSAGE.toString()),"'--'");
    		if(OrgCapchaMessage.contains(CpchaMsg[0]) & OrgCapchaMessage.contains(CpchaMsg[2])) {
    			if(!ap.getCapchaOk()) {  // If player closes inventory using ESCape key .
    				p.kickPlayer(ThunderAUtils.colorize(ChatColor.RED + Language.WRONG_CAPCHA_CLICK.toString()));
    			}
    		}
        }    	
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if(ap.getPwdOldStatus().equals("") & ap.getPwdNewStatus().equals("")) { // No Pending Password Change
        	if (ap.isLoggedIn()) return; // Only do this if Not in Pending Password menu mode.
        }
        
        // TEST MET INVENTORY PASSWORD CHECKER, zie: https://www.youtube.com/watch?v=XenOtWM597Q
        ap.setCapchaOk(true); // We have a click-event, so Capcha-click ok..
        Inventory inv = e.getInventory();
        if(inv != null) {        	
        	String CapchaMessage = e.getView().getTitle(); // Inventory Capcha Title;
        	String[] CpchaMsg = CapchaMessage.split("'");
        	if(CpchaMsg.length==3) { // We have a Inventory Title containing Single Quotes (Our Inventory)
        		// Inventory-Title CpchaMsg[0]: first part, CpchaMsg[1]: Capcha Block, CpchaMsg[2]: last part

        		String OrgCapchaMessage = String.format(ThunderAUtils.colorize(Language.CAPCHA_MESSAGE.toString()),"'--'");
            	
        		if(OrgCapchaMessage.contains(CpchaMsg[0]) & OrgCapchaMessage.contains(CpchaMsg[2])) {
        			ItemStack is = e.getCurrentItem();        			
        			if(is!=null) {
        				if(!is.getType().toString().equals(CpchaMsg[1])) {
        					p.sendMessage(ChatColor.RED + "FOUT, " + is.getType().toString() + " is aangeklikt");
        					p.kickPlayer(ThunderAUtils.colorize(ChatColor.RED + Language.WRONG_CAPCHA_CLICK.toString()));
            	            ap.setCapchaOk(false);
        				}        				
        			}
        			else
        			{
        	           ap.setCapchaOk(false); // Empty ItemStack, Login False
        			}
        		}
    		}

        	String PsswrdMessage;
        	
            PsswrdMessage = ThunderAUtils.colorize(Language.REGISTER_MESSAGE.toString()); // Password Register Using Pictogram
        	if(e.getView().getTitle().equals(PsswrdMessage)) { // Is the inventory title in createInventory
        		ItemStack is = e.getCurrentItem();        		
        		if(is!=null) {
                	Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                		public void run() { // Register using UserName+Password
                    		String PictoGram = ap.getUserName() + is.getType().toString();
                			CmdRegister.CmdRegisterMe((CommandSender) p, "Register", PictoGram);
                    	}
                	}, 1L);                		
        		}
        	}

            PsswrdMessage = ThunderAUtils.colorize(Language.PASSWORD_MESSAGE.toString()); // Password Login Using Pictogram
        	if(e.getView().getTitle().equals(PsswrdMessage)) { // Is the inventory title in createInventory
        		ItemStack is = e.getCurrentItem();        		
        		if(is!=null) {
                	Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                		public void run() {
                    		String PictoGram = is.getType().toString();
                			CmdLogin.CmdLogMeIn((CommandSender) p, "Login", PictoGram);
                    	}
                	}, 1L);                		
        		}
        	}

            PsswrdMessage = ThunderAUtils.colorize(Language.OLD_PASSWORD_MSSGE.toString()); // Clicked OLD Password Using Pictogram
        	if(e.getView().getTitle().equals(PsswrdMessage)) { // Is the inventory title in createInventory
        		ItemStack is = e.getCurrentItem();
        		if(is!=null) {
                	Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                		public void run() {
                    		String PictoGram = is.getType().toString();
                    		String OtherPswd = ap.getPwdNewStatus();
                			ap.setPwdOldStatus("menu"); // Set menuInput
                			ap.setPwdNewStatus(""); // Reset
                			CmdChngPwd.CmdChgMyPswd((CommandSender) p, "ChangePassword" , PictoGram+","+OtherPswd);
                    	}
                	}, 4L);                		
        		}
        	}
            PsswrdMessage = ThunderAUtils.colorize(Language.NEW_PASSWORD_MSSGE.toString()); // Clicked NEW Password Using Pictogram
        	if(e.getView().getTitle().equals(PsswrdMessage)) { // Is the inventory title in createInventory
        		ItemStack is = e.getCurrentItem();
        		if(is!=null) {
                	Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                		public void run() {
                    		String PictoGram = is.getType().toString();
                    		String OtherPswd = ap.getPwdOldStatus();        		
                			ap.setPwdOldStatus(""); // Reset 
                			ap.setPwdNewStatus("menu"); // Set menuInput
                			CmdChngPwd.CmdChgMyPswd((CommandSender) p, "ChangePassword" , OtherPswd+","+PictoGram);
                    	}
                	}, 4L);                		
        		}
        	}

            // Register using UserName+Password
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), (Runnable) p::closeInventory, 1L);
        	

        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onInvInteract(InventoryInteractEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if(ap.getPwdOldStatus().equals("") & ap.getPwdNewStatus().equals("")) { // No Pending Password Change
        	if (ap.isLoggedIn()) return; // Only do this if Not in Pending Password menu mode.
        	// There might be an open inventory, so no interact with the Inventory, only click-event is allowed to select
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void sameName(AsyncPlayerPreLoginEvent e) {
        if (Config.kickIfAlreadyOnline) return; // Allow Login if 'KickIfOnline'=false
        AuthPlayer ap = AuthPlayer.getAuthPlayer(e.getName());
        Player p = ap.getPlayer();
        if (p == null) return; // Allow Login if not 'OnLine'
        if (!ap.isLoggedIn()) return; // Allow Login if not 'Logged In'
        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ThunderAUtils.colorize(Language.ANOTHER_PLAYER_WITH_NAME.toString()));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void earlyPlayerJoin(PlayerLoginEvent e) {
    //public void join(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        final AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        
        // First-Spawn location somehow Eratic?? When NEW player first/initial Joins, 
        // he has a Minecraft Vanilla Join location. But after the Registration he is 
        // put at a slightly different location. This should fix this?
		if(ap.getJoinLocation() != p.getLocation()) {
			ap.setJoinLocation(p.getLocation());
		}
       
    	if(!ap.isRegistered()){
    		ap.setUserName(p.getName()); // So the PConfManager knows his PlayerName while removing..
    		String RealName = PConfManager.doesPlayerExist(p.getName());
    		if(!RealName.equals(p.getName())) {
                String StrOut = String.format(ThunderAUtils.colorize(Language.PLAYER_REGISTERED_OTHERCASE.toString()), RealName);
        		this.plugin.getLogger().info(p.getName() + ": "+ StrOut);
        		//p.kickPlayer(Language.PLAYER_ALREADY_REGISTERED.toString());
            	e.disallow(PlayerLoginEvent.Result.KICK_FULL, p.getName() + ": "+ StrOut);
        	}
    		else 
    		{
    			// Spawn off a asynchronously process to find the Number of Players from this Ip-Address 
        		new BukkitRunnable() {
        		    @Override
        		    public void run() {
                    	int PlayerCount = PConfManager.countPlayersFromIp(ap.getCurrentIPAddress()); // "192.168.1.7","Userid"
                    	// Store PlayerCount so Register command can find and check on it.
                    	ap.setPlayerIpCount(PlayerCount);
        		    }
        		}.runTaskAsynchronously(Main.getInstance());
    		}
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {

        if (this.plugin.getServer().getOnlineMode() && Config.disableIfOnlineMode) return;
        if (!Config.requireLogin) return;
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);

        if (Config.useLoginPermission && !p.hasPermission(Config.loginPermission)) return;
        ap.setLastJoinTimestamp(System.currentTimeMillis());
        ap.setJoinLocation(p.getLocation()); // Save Current (join) Location
        
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		for (String playerAction : Config.playerActionSJoin) {
			if(!playerAction.trim().isEmpty()) {
	            //Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));
				
	        	try {
	        		if(playerAction.contains("TWait("))
	        			ThunderAUtils.createRunLaterCommand(ap.getUserName(), playerAction);
	        		else
	        			Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));
		            
	        	} catch (Exception  error  ) {
	            	this.plugin.getLogger().info("Error OnJoin Executing: " + playerAction.replace("$P", ap.getUserName()) );
	            	error.printStackTrace();
	        	}

			}
		}
        
    	if (ap.isWithinSession() & ap.isRegistered()) {
        	if(ap.isVIP()){ // is on nlplist 
        		p.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.LOGGED_IN_VIA_NLPLIST.toString())); // NLPLIST!!
        		this.plugin.getLogger().info(p.getName() + " "+ ThunderAUtils.colorize(Language.WAS_LOGGED_IN_VIA_NLPLIST.toString()));
        	}	
        	else
        		{
                if(p.hasPermission("thunderauthentication.nlpwd")) { // has authorization
        			p.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.LOGGED_IN_VIA_NLPAUTH.toString())); // NLPAUTH!!
        			this.plugin.getLogger().info(p.getName() + " "+ ThunderAUtils.colorize(Language.WAS_LOGGED_IN_VIA_NLPAUTH.toString()));
                }
                else
        			{
                	if ((Config.sessionsEnabled)) { // Just normal default session time
                		p.sendMessage(ChatColor.BLUE + ThunderAUtils.colorize(Language.LOGGED_IN_VIA_SESSION.toString()));
                		this.plugin.getLogger().info(p.getName() + " "+ ThunderAUtils.colorize(Language.WAS_LOGGED_IN_VIA_SESSION.toString()));
                	}
        		}
        	}
        	ap.enableAfterLoginGodmode();
        	ap.setLoggedIn(true);

            if((ap.getEmailAddress().equals("") | ap.getEmailAddress().contains("#")) && Config.emailForceSet) {
				ap.createSetEmailReminder(this.plugin);
        	}

    		for (String playerAction : Config.playerActionSJGrc) {
    			if(!playerAction.trim().isEmpty()) {
    	            //Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));
    				
    	        	try { 
   		        		if(playerAction.contains("TWait("))
   		        			ThunderAUtils.createRunLaterCommand(ap.getUserName(), playerAction);
   		        		else
   		        			Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));

    	        		
    	        	} catch (Exception  error  ) {
    	            	this.plugin.getLogger().info("Error OnGrace Executing: " + playerAction.replace("$P", ap.getUserName()) );
    	            	error.printStackTrace();
    	        	}

    			}
    		}

    		return;
        }

        ap.logout(this.plugin, true); // Illegal Login, just 'Logout' ..

        if (Config.useHideInventory) ap.HideSurvivalInventory(p);

        // TEST MET INVENTORY PASSWORD CHECKER, zie: https://www.youtube.com/watch?v=XenOtWM597Q
        
        // Player is "Logged-Out", so: If Config.UseCapcha: Show Capcha-Inventory 
    	if(Config.UseCapcha) {
            ap.setCapchaOk(false); // We hebben een click-event, dus Capcha-click ok..
    		//Material KlickBlock = Material.DIRT; // Has to be a random name from predefined  list.
    		int RandBlock = ThunderAUtils.getRandom(0,  ThunderAUtils.MaxBlockCnt-1);
    		Material KlickBlock =  ThunderAUtils.Blocks[RandBlock];
    		
    		String CapchaMessage = String.format(ThunderAUtils.colorize(Language.CAPCHA_MESSAGE.toString()),"'"+KlickBlock.toString()+"'");

    		
    		long WaitTime = 20L; // WaitTime on Server-ChunkLoad..
        	Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
        		public void run() {
    	           	//p.openInventory(inv);

    	        	ThunderAUtils.showCapchaPopup(p, WaitTime, CapchaMessage,KlickBlock);
            	}
        	}, WaitTime); // wait WaitTime ticks on player-server login completion                		

    	}
    }

    @EventHandler
    public void godModeAfterLogin(EntityDamageEvent e) {
        if (!Config.godModeAfterLogin) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (!ap.isInAfterLoginGodmode()) return;
        e.setDamage(0);
        e.setCancelled(true);
    }

    @EventHandler
    public void onExit(PlayerQuitEvent e) {

        AuthPlayer ap = AuthPlayer.getAuthPlayer(e.getPlayer());
        if (Config.useHideInventory) ap.RestoreSurvivalInventory(e.getPlayer());

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        for (String playerAction : Config.playerActionLeave) {
			if(!playerAction.trim().isEmpty()) {
	            //Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));

	        	try {
	        		if(playerAction.contains("TWait("))
	        			ThunderAUtils.createRunLaterCommand(ap.getUserName(), playerAction);
	        		else
	        			Bukkit.dispatchCommand(console, playerAction.replace("$P", ap.getUserName()));
	        		
	        	} catch (Exception  error  ) {
	            	this.plugin.getLogger().info("Error OnExit Executing: " + playerAction.replace("$P", ap.getUserName()) );
	            	error.printStackTrace();
	        	}

			}
		}

        if (!Config.sessionsEnabled) return;

        ap.setLastQuitTimestamp(System.currentTimeMillis());
        BukkitTask reminder = ap.getCurrentReminderTask();
        if (reminder != null) reminder.cancel();
        if (ap.isLoggedIn()) ap.updateLastIPAddress();
    }

    @EventHandler
    public void kick(PlayerKickEvent e) {
        onExit(new PlayerQuitEvent(e.getPlayer(), e.getLeaveMessage()));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) throws SQLException {
        if (Config.allowMovementWalk) return;
        Player p = e.getPlayer();
        Player player = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;

        Statement st = SQL.getConnection().createStatement();
        Statement stmt = SQL.getConnection().createStatement();

        try {
            String query = "SELECT * FROM " + player.getName();

            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                String playerName = rs.getString("name");
                String playerUUID = rs.getString("uuid");
                String checkPremium = rs.getString("premium");
                String actualUUIDStr = String.valueOf(MojangAPI.getUUID(player.getName()));

                if((playerName.equals(player.getName()))){
                    if(!(playerUUID.equals(actualUUIDStr))){
                        if(GetConfig.getPremiumLanguageEN()){
                            player.kickPlayer(Format.color(MessagesEN.spoofedPlayerEN())); // Hacker
                        }
                        if(GetConfig.getPremiumLanguageIT()){
                            player.kickPlayer(Format.color(MessagesIT.spoofedPlayerIT())); // Hacker
                        }
                    } else {
                        if((playerUUID.contains(actualUUIDStr))) {
                            if (checkPremium.equals("SI")) {
                                e.setCancelled(false);
                                p.setCollidable(true);
                                ap.login();
                                rs.close();
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        p.setCollidable(false);   		
        
        Location to = e.getTo();
        Location from = e.getFrom();
        boolean walked = to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ();
        if(Config.allowMovementTime>0) {
 			//p.sendMessage(ChatColor.BLUE + "Time: " + ap.getLastJoinTimestamp() + " " +Config.allowMovementTime + " " + System.currentTimeMillis());
            if(walked && ap.getLastWalkTimestamp()+Config.allowMovementTime<=System.currentTimeMillis()) {
     			//p.sendMessage(ChatColor.BLUE + "Reset? " + (ap.getLastJoinTimestamp() + Config.allowMovementTime) + " " + System.currentTimeMillis());
            	if(ap.getJoinLocation()==null) ap.setJoinLocation(e.getFrom());
           		e.setTo(ap.getJoinLocation());
            	ap.setLastWalkTimestamp(System.currentTimeMillis()); // next allowed walk timeout
            }
        }
        else
        	if (walked || !Config.allowMovementLook) e.setTo(e.getFrom());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        final AuthPlayer ap = AuthPlayer.getAuthPlayer(p);

    	String Cmnd;
    	String Parm;
        String m = e.getMessage();
        m = m.replaceAll("  ", " ").trim(); // No DoubleSpaces and spaces
        int i = m.indexOf(' ');
        if(i==-1) {
        	Cmnd = m;
        	Parm = "";
        }
        else{
        	Cmnd = m.substring(0, i);
        	Parm = m.substring(i).trim();
        }
		//p.sendMessage("+++" + m + "+++");
		//p.sendMessage("+++" + Cmnd + "+++");
		//p.sendMessage("+++" + Parm + "+++");
        if (" \\l \\li \\login \\logon ".contains(" "+Cmnd+" ")){
            Bukkit.getScheduler().runTask(plugin, () -> CmdLogin.CmdLogMeIn((CommandSender) p, "login", Parm));
            	
            e.setCancelled(true);
            return;

        }
    
        if (" \\lo \\logoff \\logout ".contains(" "+Cmnd+" ")){
            Bukkit.getScheduler().runTask(plugin, () -> CmdLogout.CmdLogMeOff((CommandSender) p, "logout"));
            	
            e.setCancelled(true);
            return;

        }
        if (" \\reg \\register ".contains(" "+Cmnd+" ")){
            Bukkit.getScheduler().runTask(plugin, () -> CmdRegister.CmdRegisterMe((CommandSender) p, "register", Parm));
            	
            e.setCancelled(true);
            return;


        }
        if (" \\cpwd \\changepassword \\changepass \\passchange ".contains(" "+Cmnd+" ")){
            Bukkit.getScheduler().runTask(plugin, () -> CmdChngPwd.CmdChgMyPswd((CommandSender) p, "changepassword", Parm));
            	
            e.setCancelled(true);
            return;

        }
        if (" \\setemail ".contains(" "+Cmnd+" ")){
            Bukkit.getScheduler().runTask(plugin, () -> CmdSetEmail.CmdSetMyMail((CommandSender) p, "setemail", Parm));
            	
            e.setCancelled(true);
            return;
        } 

        if (" \\recoverpwd ".contains(" "+Cmnd+" ")){
            Bukkit.getScheduler().runTask(plugin, () -> CmdRecover.CmdSendNewPwd((CommandSender) p, "recoverpwd"));
            	
            e.setCancelled(true);
            return;
        } 

        // -----------------------------------------------------------------------------------
        
        if (ap.isLoggedIn()) return; // LoggedIn, All is well
        if (!Config.allowChat) {
            e.setCancelled(true);
            return;
        }
        e.setMessage(ThunderAUtils.colorize(Config.chatPrefix) + e.getMessage());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
    	// Hier moeten we ooit een keer iets toevoegen zodat het LogIn Event GEEN CONSOLE/LOG melding geeft!!!
        if (Config.allowCommands) return;
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        String[] split = e.getMessage().split(" ");
        if (split.length < 1) {
            p.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.YOU_MUST_LOGIN.toString()));
            return;
        }
        String root = split[0].substring(1); // the command label (remove /)
        for (String allowed : Config.allowedCommands) {
            if (!allowed.equalsIgnoreCase(e.getMessage().substring(1))) continue;
            return;
        }
        PluginCommand pc = this.plugin.getCommand(root);
        if (pc == null) {
            pc = this.plugin.getServer().getPluginCommand(root);
            if (pc != null) {
                if (Config.allowedCommands.contains(pc.getName())) return;
                for (String alias : pc.getAliases()) if (Config.allowedCommands.contains(alias)) return;
            }
            p.sendMessage(ChatColor.RED + ThunderAUtils.colorize(Language.YOU_MUST_LOGIN.toString()));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!Config.godMode) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setDamage(0);
        e.setCancelled(true);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (!Config.validateUsernames) return;
        Player p = e.getPlayer();
        if (p.getName().matches(Config.usernameRegex)) return;
        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        e.setKickMessage(ThunderAUtils.colorize(Language.INVALID_USERNAME.toString()));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityTargetPlayer(EntityTargetEvent  e) {
    	if (!(e.getTarget() instanceof Player)) return;
        Player p = (Player) e.getTarget();
   		AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
   		if (ap.isLoggedIn()) return;
   		if (Config.adventureMode) return;
        //p.sendMessage(ChatColor.RED + "EntityTargetEvent from " + e.getEntity() + " to " + ap.getUserName() + " Cancelled");
   		e.setCancelled(true);
    }

    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }


    @EventHandler
    public void sign(SignChangeEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void blockDamage(BlockDamageEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void enchantItem(EnchantItemEvent e) {
        Player p = e.getEnchanter();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent e) {
        Player p = e.getEnchanter();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void playerPortal(PlayerPortalEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler 
    //public void onPickup(PlayerPickupItemEvent e) {
    //   Player p = e.getPlayer();
    //   AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
    //   if (ap.isLoggedIn()) return;
    //   e.setCancelled(true);
    //}
    public void onPickup(EntityPickupItemEvent e) {
       if(e.getEntity() instanceof Player) {
    	   AuthPlayer ap = AuthPlayer.getAuthPlayer((Player) e.getEntity());
    	   if (ap.isLoggedIn()) return;
    	   e.setCancelled(true);
       }
    }

    @EventHandler
    public void onBreakHanging(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player)) return;
        Player p = (Player) e.getRemover();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlaceHanging(HangingPlaceEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onAnimate(PlayerAnimationEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEnterBed(PlayerBedEnterEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEmpty(PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onFill(PlayerBucketFillEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onIntEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void toggleSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void toggleFly(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void toggleSprint(PlayerToggleSprintEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void enterVehicle(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player)) return;
        Player p = (Player) e.getEntered();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void exitVehicle(VehicleExitEvent e) {
        if (!(e.getExited() instanceof Player)) return;
        Player p = (Player) e.getExited();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void armorManipulate(PlayerArmorStandManipulateEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void itemMending(PlayerItemMendEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }
    
    @EventHandler
    public void unleashEntity(PlayerUnleashEntityEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }
    
    @EventHandler
    public void swapHandItem(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void editBook(PlayerEditBookEvent e) {
        Player p = e.getPlayer();
        AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
        if (ap.isLoggedIn()) return;
        e.setCancelled(true);
    }
    
}

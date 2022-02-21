package com.md_4.thunderauthentication;

import com.google.common.io.PatternFilenameFilter;
import com.md_4.thunderauthentication.backend.core.engine.*;
import com.md_4.thunderauthentication.backend.core.engine.commands.*;
import com.md_4.thunderauthentication.backend.core.engine.tools.MySQL;
import com.md_4.thunderauthentication.backend.core.engine.tools.QueueClass;
import com.md_4.thunderauthentication.commands.AuthPremium;
import com.md_4.thunderauthentication.commands.UnPremium;
import com.md_4.thunderauthentication.events.AutomaticAuth;
import com.md_4.thunderauthentication.security.OnlyProxyJoin;
import com.md_4.thunderauthentication.security.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

// To publish JavaCode on bukkit.org:
// Use these tags: [syntax=java]code here[/syntax]

public class Main extends JavaPlugin {

    // AntiUUIDSpoof
    private static UUIDManager uuidManager = null;
    private static OnlyProxyJoin onlyProxyJoin = null;

    // Check If Premium

    public static HashMap<UUID, String> premiumCheck = new HashMap<>();

    // Start Imports
    public static MySQL SQL;
    // End Imports

    public static File dataFolder;
    public Config c;
    public Logger log;
    //public LogFilter MyFilter;
    private BukkitTask reminderTask = null;

    private static int MaxQueueDepth = 500; // This should be enough to handle size 10.000 player base??
    public static QueueClass MyQueue = new QueueClass(MaxQueueDepth);

    private static Main instance;
    private static int pluginID;
    public Main() {
        instance = this;
        pluginID = 14429;
    }

    public Metrics BSMetrics(){
        return new Metrics(this, pluginID);
    }

    public static Integer getPluginID(){
        return pluginID;
    }


    public static Plugin getInstance() {
        // TODO Auto-generated method stub
        return instance;
    }

    private BukkitTask getCurrentReminderTask() {
        return this.reminderTask;
    }

    /**
     * Creates a PlayerConfigSave task.
     *
     * @param p Plugin to register task under
     * @return Task created
     */

    private BukkitTask createSaveTimer(Plugin p) {
        this.reminderTask = ThunderAUtils.createSaveTimer(p);
        return this.getCurrentReminderTask();
    }

    /**
     * Creates a PlayerConfigSave task.
     *
     * @param p Plugin to register task under
     * @return Task created
     */

    private BukkitTask createChkVersionTimer(Plugin p) {
        this.reminderTask = ThunderAUtils.createChkVersionTimer(p);
        return this.getCurrentReminderTask();
    }

    /**
     * Registers a command in the server. If the command isn't defined in plugin.yml
     * the NPE is caught, and a warning line is sent to the console.
     *
     * @param ce      CommandExecutor to be registered
     * @param command Command name as specified in plugin.yml
     * @param jp      Plugin to register under
     */
    private void registerCommand(CommandExecutor ce, String command, JavaPlugin jp) {
        try {
            jp.getCommand(command).setExecutor(ce);
        } catch (NullPointerException e) {
            jp.getLogger().warning(String.format(ThunderAUtils.colorize(Language.COULD_NOT_REGISTER_COMMAND.toString()), command, e.getMessage()));
        }
    }

    private void update() {
        final File userdataFolder = new File(dataFolder, "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) return;
        this.getLogger().info("Checking+Updating old player profilefiles to new format (may take a long time)");
        for (String fileName : userdataFolder.list(new PatternFilenameFilter("(?i)^.+\\.yml$"))) {
            String playerName = fileName.substring(0, fileName.length() - 4); // ".yml" = 4
            try {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString(playerName);
                continue; // FileName(PlayerName) sounds like a UUID, no need to rename!!
            } catch (IllegalArgumentException ignored) {}

            UUID u;
            try {
                u = ThunderAUtils.getUUID(playerName);
            } catch (Exception ex) {
                //ex.printStackTrace();
                u = null;
            }

            if (u == null) {
                this.getLogger().warning(ThunderAUtils.colorize(Language.ERROR.toString()));
                continue;
            }

            // Original FileRename Code does not work, this one (below) works
            File origFile = new File(userdataFolder.toString() + File.separator + fileName);
            File destFile = new File(userdataFolder.toString() + File.separator + u + ".yml");
            if (!origFile.exists()){
                this.getLogger().info("Debug: Orig-File " + origFile.toString() + " does NOT exist??");
            }
            if (destFile.exists()){
                this.getLogger().info("Debug: Dest-File " + destFile.toString() + " allready exists??");
            }
            if (origFile.renameTo(destFile)) {
                this.getLogger().info(String.format(ThunderAUtils.colorize(Language.CONVERTED_USERDATA.toString()), fileName, u + ".yml"));
            } else {
                this.getLogger().warning(String.format(ThunderAUtils.colorize(Language.COULD_NOT_CONVERT_USERDATA.toString()), fileName, u + ".yml"));
            }

        }
    }

    //@SuppressWarnings("unused")
    private void updateToMySQL() {
        final File userdataFolder = new File(Main.dataFolder, "userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) return;

        boolean ImportError=false;

        long  PlayerJoin;
        long  PlayerQuit;
        long  PlayerLogin;
        String  PlayerName;
        String  PassWord;
        String  PassWordHash;
        String  IpAddress;
        String  EmAddress;
        boolean LoggedIn;
        boolean VipPlayer;
        long  Expire;

        int NoValidPlayerCount=0;

        this.getLogger().info("Checking+Inserting player profilefiles into MySQL (may take a long time)");
        for (String fileName : userdataFolder.list(new PatternFilenameFilter("(?i)^.+\\.yml$"))) {
            PlayerJoin=0;
            PlayerQuit=0;
            PlayerLogin=0;
            PlayerName="";
            PassWord="";
            PassWordHash="";
            IpAddress="";
            EmAddress="";
            LoggedIn=false;
            VipPlayer=false;
            Expire=0;

            Scanner in;

            ImportError=false;

            try {
                in = new Scanner(new File(userdataFolder + File.separator + fileName));
                //while (in.hasNextLine()) { // iterates each line in the file
                while (in.hasNext()) { // 1 more character?: iterates each line in the file
                    String line = in.nextLine();

                    // Timestamps:
                    if(line.contains("  join:")) 	PlayerJoin  = Long.parseLong(line.substring(line.lastIndexOf(" ")+1));
                    if(line.contains("  quit:")) 	PlayerQuit  = Long.parseLong(line.substring(line.lastIndexOf(" ")+1));
                    if(line.contains("  login:"))	PlayerLogin = Long.parseLong(line.substring(line.lastIndexOf(" ")+1));

                    // login:
                    if(line.contains("  logged_in:") && line.contains("true")) LoggedIn=true;
                    if(line.contains("  username:"))  PlayerName   = line.substring(line.lastIndexOf(" ")+1);
                    if(line.contains("  password:"))  PassWord     = line.substring(line.lastIndexOf(" ")+1);
                    if(line.contains("  hash:"))      PassWordHash = line.substring(line.lastIndexOf(" ")+1);
                    if(line.contains("  ipaddress:")) IpAddress    = line.substring(line.lastIndexOf(" ")+1);
                    if(line.contains("  emaddress:")) EmAddress    = line.substring(line.lastIndexOf(" ")+1);
                    if(line.contains("  vip:") && line.contains("true")) VipPlayer=true;
                    if(line.contains("godmode_expires:")) Expire = Long.parseLong(line.substring(line.lastIndexOf(" ")+1));

                    if(PlayerName.equals("")) {
                        NoValidPlayerCount++;
                        PlayerName="--NoName-" + String.valueOf(NoValidPlayerCount);
                    }
                }
                in.close(); // don't forget to close resource leaks
            } catch (FileNotFoundException|NumberFormatException e) {
                // TODO Auto-generated catch block
                ImportError=true;
                this.getLogger().info("Error in file: " + userdataFolder + File.separator + fileName);
                e.printStackTrace();
            }
            if(!ImportError) {
                // THIS IMPORTS/Inserts Removed Players From Profile-files into the database
                PreparedStatement ps;
                try {
                    int RecAanwezig=0;
                    String PlayerUUID =fileName.split("\\.")[0];
                    //Connection con = MySQL.getConnection();
                    ps = MySQL.getConnection().prepareStatement(
                            "SELECT count(*) as Aanwezig " +
                                    "FROM Players " +
                                    "WHERE UUID = ?"
                    );
                    ps.setString(1, PlayerName); // 1 is the first "?" in the SQL string
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() == true) {
                        RecAanwezig=rs.getInt("Aanwezig");
                    }
                    ps.close();
                    rs.close();
                    if(RecAanwezig==0) {
                        ps = MySQL.getConnection().prepareStatement(
                                "INSERT IGNORE INTO Players " +
                                        "       ( Name, UUID, Joyn, Quit, Login, LoggedIn, Password, Hash, IpAdress, EmlAdress, Vip, GodModeEx) " +
                                        "VALUES (  ?  ,  ?  ,  ? ,   ? ,   ?  ,     ?   ,     ?   ,   ? ,     ?    ,    ?     ,  ? ,     ?    )"
                        );
                        ps.setString(   1,  PlayerName);
                        ps.setString(   2,  PlayerUUID);
                        ps.setLong(     3,  PlayerJoin);
                        ps.setLong(     4,  PlayerQuit);
                        ps.setLong(     5,  PlayerLogin);
                        ps.setBoolean(  6,  LoggedIn);
                        ps.setString(   7,  PassWord);
                        ps.setString(   8,  PassWordHash);
                        ps.setString(   9,  IpAddress);
                        ps.setString(  10,  EmAddress);
                        ps.setBoolean( 11,  VipPlayer);
                        ps.setLong(    12,  Expire);
                        ps.executeUpdate();
                        ps.close();
                    }
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveLangFile(String name) {
        if (!new File(this.getDataFolder() + File.separator + "lang" + File.separator + name + ".properties").exists())
            this.saveResource("lang" + File.separator + name + ".properties", false);
    }

    // Define Configuration
    public void Config() {
        File lang = new File(this.getDataFolder(), "premium");
        File lang_it = new File(this.getDataFolder() + "/premium/it_IT.yml");
        File lang_en = new File(this.getDataFolder() + "/premium/it_EN.yml");
        if (!lang.exists()) {
            lang.mkdir();
        }

        FileConfiguration lang_it_config = YamlConfiguration.loadConfiguration(lang_it);
        FileConfiguration lang_en_config = YamlConfiguration.loadConfiguration(lang_en);
        if (!lang_it.exists()) {
            try {
                lang_it.createNewFile();
                lang_it_config.createSection("Messages");
                lang_it_config.set("Messages.Prefix", "&7[&cThunderAuthenticator&7] ");
                lang_it_config.set("Messages.NotPremium", "&cDevi aver acquistato Minecraft per utilizzare questo comando.");
                lang_it_config.set("Messages.SpoofedPlayer", "&cUUIDSpoof Detectato, Account Bloccato.");
                lang_it_config.set("Messages.Error", "&cErrore Anomalo Del Sistema Riscontrato, Contatta &4md_4#7401 &cPer Informazioni.");
                lang_it_config.set("Messages.PremiumAdded", "&aAccount PREMIUM Registrato, da ora in poi sarai AUTENTICATO Automaticamente.");
                lang_it_config.set("Messages.AlreadyPremium", "&aSei giá nella lista Premium.");
                lang_it_config.set("Messages.TableNotFound", "&cTabella non trovata nel database.");
                lang_it_config.set("Messages.TableDeleted", "&aTabella cancellata con successo.");
                lang_it_config.set("Messages.AccessDenied", "&cAccesso Negato");
                lang_it_config.set("Messages.PremiumDisabled", "&c[MySQL] Autenticazione Premium (/premium) Disabilitato su config.yml");
                lang_it_config.set("Messages.UpdatedUUID", "&cUUID Aggiornato, Rientra Nel Server Perfavore.");
                lang_it_config.set("Messages.Usage", "&eUtilizza /unpremium <NomePlayer>");
                lang_it_config.save(lang_it);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (!lang_en.exists()) {
            try {
                lang_en.createNewFile();
                lang_en_config.createSection("Messages");
                lang_en_config.set("Messages.Prefix", "&7[&cThunderAuthenticator&7] ");
                lang_en_config.set("Messages.NotPremium", "&cYou must have purchased Minecraft to use this command.");
                lang_en_config.set("Messages.SpoofedPlayer", "&cUUIDSpoof Detected, Account Blocked.");
                lang_en_config.set("Messages.Error", "&cAnomalous System Error Encountered, Contact &4md_4#7401 &cFor information.");
                lang_en_config.set("Messages.PremiumAdded", "&aPREMIUM Account Registered, from now on you will be AUTHENTICATED Automatically.");
                lang_en_config.set("Messages.AlreadyPremium", "&aYou are already on the Premium list.");
                lang_en_config.set("Messages.TableNotFound", "&cTable not found in the database.");
                lang_en_config.set("Messages.TableDeleted", "&aTable successfully deleted.");
                lang_en_config.set("Messages.AccessDenied", "&cAccesso Negato");
                lang_en_config.set("Messages.PremiumDisabled", "&c[MySQL] Premium Authentication (/premium) Disabled On config.yml");
                lang_en_config.set("Messages.UpdatedUUID", "&cUUID Updated, Re-enter Server Please.");
                lang_en_config.save(lang_en);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Define MySQL Connection
    public void DataBase(){
        try {
            SQL = new MySQL();

            if(getConfig().getString("saving.mysql_filemode").equals("Backup") || getConfig().getString("saving.mysql_filemode").equals("")){
                SQL.connect();
            } else {
                SQL.disconnect();
            }

            if(SQL.isConnected()){
                System.out.println("[MySQL] Pulling MySQL Requests From ThunderAuthentication...");
            }
        } catch (Exception ex){
            Bukkit.getLogger().warning("[MySQL] Abnormal Error the error (if you are generating the configuration for the first time do not worry it's normal)");
        }
    }


    public static UUIDManager getUUIDManager() {
        return uuidManager;
    }

    public static OnlyProxyJoin getOnlyProxyJoin() {
        return onlyProxyJoin;
    }

    @Override
    public void onEnable() {
        DataBase();
        getConfig().options().copyHeader(true);
        uuidManager = new UUIDManager();
        onlyProxyJoin = new OnlyProxyJoin(this);
        onlyProxyJoin.init();
        Main.dataFolder = this.getDataFolder();

        if (!new File(getDataFolder(), "config.yml").exists()) this.saveDefaultConfig();

        this.c = new Config(instance); // Is deze nodig ??, 'c' wordt niet gebruikt??
        this.log = this.getLogger();

        this.saveLangFile("en_us");
        this.saveLangFile("it_it");
        Config();
        try {
            new Language.LanguageHelper(new File(this.getDataFolder(), this.getConfig().getString("general.language_file", "lang/en_us.properties")));
        } catch (IOException e) {
            this.log.severe("Could not load language file: " + e.getMessage());
            this.log.severe("Disabling plugin.");
            this.setEnabled(false);
            return;
        }

        if (!Config.MySqlDbHost.equals("")) MySQL.connect(); // Connect to the Database System

        if (Config.checkOldUserdata) {
            this.update();
        }
        // We only do the updateToMySQL() if MySqlDbHost is used and we are in "BacKUp" mode.
        // The update-ProfileFiles-To-MySQL only does its trick when the table is empty thought.!!!
        // So the existing info will not be overwritten by possible incorrect data on server-start.
        if (!Config.MySqlDbHost.equals("") & Config.MySqlDbFile.equals("Backup")) {
            int Aantal=0;
            PreparedStatement ps;
            try { // Force MySQL connection to be Active, Just doing nothing except renew MySql Connection
                ps = MySQL.getConnection().prepareStatement(
                        "SELECT count(*) as Aantal " +
                                "FROM   Players " );
                ResultSet res = ps.executeQuery();
                //Code using ResultSet entries here
                if (res.next() == true) {
                    Aantal = res.getInt("Aantal");
                }
                res.close();
                ps.close();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if(Aantal == 0) this.updateToMySQL(); // The update-ProfileFiles-To-MySQL only when the table is empty!!!
        }

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new AuthListener(this), this);
        pm.registerEvents(new AutomaticAuth(this), this);

        this.registerCommand(new AuthPremium(this), "thunderauthenticationpremium", this);
        this.registerCommand(new UnPremium(this), "thunderauthenticationunpremium", this);
        this.registerCommand(new ThunderAuth(this), "thunderauthentication", this);
        this.registerCommand(new CmdLogin(this), "login", this);
        this.registerCommand(new CmdLogout(this), "logout", this);
        this.registerCommand(new CmdRegister(this), "register", this);
        this.registerCommand(new CmdSetEmail(this), "setemail", this);
        this.registerCommand(new CmdChngPwd(this), "changepassword", this);
        this.registerCommand(new CmdRecover(this), "recoverpwd", this);

        for (Player p : this.getServer().getOnlinePlayers()) {
            AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
            if (ap.isLoggedIn()) continue;
            if (ap.isRegistered()) ap.createLoginReminder(this);
            else ap.createRegisterReminder(this);
        }

        // Create the Bukkit-Timer to save the UserData on regular intervals
        if(this.createSaveTimer(this)==null){ // Uses (see top): public BukkitTask createSaveTimer(Plugin p)
            this.getLogger().info(ChatColor.RED + "AutoSave Task not created!");
        }
        else
        {
            if(Config.removeAfterDays>0)
                this.getLogger().info("Auto removal of inactive playerdata set to " + Config.removeAfterDays +" days old!");
            else
                this.getLogger().info("Auto removal of inactive playerdata disabled!");
        }

        this.getLogger().info("Counting PlayerBase, IP-Addresses and nlp-players.");
        PConfManager.countPlayersFromIpAndGetVipPlayers();
        this.getLogger().info("Counting done, PlayerBaseCount: " + String.valueOf(PConfManager.getPlayerCount() ) +
                ", Ip-AddressCount: " + String.valueOf(PConfManager.getIpaddressCount() ) +
                ", nlp-PlayerCount: " + String.valueOf(PConfManager.getVipPlayerCount() ) +
                "." );

        ThunderAuth.CheckDevMessage(Bukkit.getConsoleSender());

        if(this.createChkVersionTimer(this)==null){ // Uses (see top): public BukkitTask createChkVersionTimer(Plugin p)
            this.getLogger().info("Check-ThunderAuthentication-Version Task not created!");
        }

        if (getConfig().getBoolean("general.metrics_enabled"))
        {
            ////-- Hidendra's Metrics --//
            //try {
            //	Metrics metrics = new Metrics(this);
            //	if (!metrics.start()) this.getLogger().info(Language.METRICS_OFF.toString());
            //	else this.getLogger().info(Language.METRICS_ENABLED.toString());
            //} catch (Exception ignore) {
            //	// Failed to submit the stats :-(
            //	this.getLogger().warning(Language.COULD_NOT_START_METRICS.toString());
            //}

            // All you have to do is adding this line in your onEnable method:
            //@SuppressWarnings("unused")

            //Metrics metrics = new Metrics(this);
            //this.getLogger().info(Language.METRICS_ENABLED.toString());

            int pluginId = 14429; // <-- Replace with the id of your plugin!

            Metrics metrics = new Metrics(this,pluginId);

            this.getLogger().info(ThunderAUtils.colorize(Language.METRICS_ENABLED.toString()));

            // Optional: Add custom charts
            metrics.addCustomChart(new Metrics.SimplePie("Registered_PlayerCount", new Callable<String>() {
                @Override
                public String call() throws Exception {
                    int PlayCnt=(PConfManager.getPlayerCount()/100);
                    return String.valueOf(PlayCnt*100) + "-" + String.valueOf(((PlayCnt+1)*100)-1) ;
                    //return "My value";
                }
            }));

            // Optional: Add custom charts
            //metrics.addCustomChart(new Metrics.SimplePie("Appl_Usage", new Callable<String>() {
            //	@Override
            //	public String call() throws Exception {
            //   	String Usage="le+";
            //		if(Config.MySqlDbHost.equals(""))
            //			Usage = Usage + "SFLO"; // SaveFileOnly
            //		else
            //    		if(Config.MySqlDbFile.equals(""))
            //    			Usage = Usage + "SDBO"; // SaveDBOnly
            //    		else
            //    			Usage = Usage + "SD&F"; //SaveDB+Fil
            //
            //		return Usage;
            //		//return "My value";
            //	}
            //}));

            // Optional: Add custom charts
            metrics.addCustomChart(new Metrics.AdvancedPie("Tools_Usage", new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() throws Exception {

                    Map<String, Integer> valueMap = new HashMap<>();

                    if(Config.MySqlDbHost.equals(""))
                        valueMap.put("SaveFileOnly", 1);
                    else
                    if(Config.MySqlDbFile.equals(""))
                        valueMap.put("DatabaseOnly", 1);
                    else
                        valueMap.put("Database+filebackup", 1);


                    if(Config.registrationType.equals("password"))
                        valueMap.put("Register-Pswrd", 1);
                    else
                        valueMap.put("Register-Email", 1);

                    if(Config.checkOldUserdata)
                        valueMap.put("CheckOldUserData", 1);

                    if(Config.emailForceSet)
                        valueMap.put("Force-SetEmail", 1);

                    if(Config.sessionType.contains("HiddenChat"))
                        valueMap.put("HiddenChat", 1);
                    else
                        valueMap.put("Commands", 1);

                    if(Config.UseCapcha)
                        valueMap.put("Using-Capcha", 1);

                    if(Config.ShowMenuOption)
                        valueMap.put("MenuOptionHint", 1);

                    if(Config.UseAutoMenu)
                        valueMap.put("Using-AutoMenu", 1);

                    if(Config.invisibleMode)
                        valueMap.put("Login-Invisible", 1);

                    if(Config.adventureMode)
                        valueMap.put("Login-Adventure", 1);

                    if (Config.useHideInventory)
                        valueMap.put("Using-HideInventory", 1);


                    if(Config.teleportToSpawn)  {
                        if(!Config.spawnWorld.equals(""))
                            valueMap.put("OnJoin=Tp2World", 1);
                        else
                            valueMap.put("Tp2Spawn", 1);
                        if(Config.useSpawnAt)
                            valueMap.put("Tp2SpawnAt", 1);
                    }

                    int Pa = 0;
                    for (String playerAction : Config.playerActionSJoin) {
                        if(!playerAction.trim().isEmpty()) Pa++;
                    }
                    if(Pa>0) valueMap.put("PaJoinCmd", Pa);

                    Pa = 0;
                    for (String playerAction : Config.playerActionSJReg) {
                        if(!playerAction.trim().isEmpty()) Pa++;
                    }
                    if(Pa>0) valueMap.put("PaJoinRegisterCmd", Pa);

                    Pa = 0;
                    for (String playerAction : Config.playerActionSJGrc) {
                        if(!playerAction.trim().isEmpty()) Pa++;
                    }
                    if(Pa>0) valueMap.put("PaJoinGraceCmd", Pa);

                    Pa = 0;
                    for (String playerAction : Config.playerActionLogin) {
                        if(!playerAction.trim().isEmpty()) Pa++;
                    }
                    if(Pa>0) valueMap.put("PaLoginCmd", Pa);

                    Pa = 0;
                    for (String playerAction : Config.playerActionLogof) {
                        if(!playerAction.trim().isEmpty()) Pa++;
                    }
                    if(Pa>0) valueMap.put("PaLogofCmd", Pa);

                    Pa = 0;
                    for (String playerAction : Config.playerActionLeave) {
                        if(!playerAction.trim().isEmpty()) Pa++;
                    }
                    if(Pa>0) valueMap.put("PaLeaveCmd", Pa);

                    return valueMap;
                }
            }));

            //// Optional: Add custom charts
            //metrics.addCustomChart(new Metrics.SimplePie("Server_Type", new Callable<String>() {
            //	@Override
            //	public String call() throws Exception {
            //		return getServer().getVersion();  // Has to show like: Spigot, Paper, eg.
            //		//return getServer().getBukkitVersion();  // Has to show like: Spigot, Paper, eg.
            //	}
            //}));

        }
        else
        {
            //this.getLogger().info("Metrics on plugin disabled.");
            this.getLogger().info(ThunderAUtils.colorize(Language.METRICS_OFF.toString()));
        }

        this.log.info("Server getVersion() reports: " + getServer().getVersion());  // Has to show like: Spigot, Paper, eg.


        this.log.info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " " + ThunderAUtils.colorize(Language.ENABLED.toString()) + ".");

        Filter MyFilter = new Filter()
        {
            public boolean isLoggable(LogRecord line) {
                //Bukkit.getServer().getLogger().info("Debug, Command-Line-Logger: " + line.getMessage());
                Bukkit.getServer().getConsoleSender().sendMessage("Send: " +line.getMessage());
                if (line.getMessage().contains("/login") || line.getMessage().contains("/register") || line.getMessage().contains("/pass")) {
                    return false;
                }
                return true;
            }
        };
        //Bukkit.getLogger().setFilter(MyFilter);
        Bukkit.getServer().getLogger().setFilter(MyFilter);
        //this.getServer().getLogger().setFilter(MyFilter);

        //Bukkit.getServer().getLogger().setFilter(new McFilter());
    }

    @Override
    public void onDisable() {
        SQL.disconnect(); // Disconnect From MySQL
        uuidManager = null; // Disconnect UUIDManager
        onlyProxyJoin = null; // Disconnect Proxy
        this.getServer().getScheduler().cancelTasks(this);

        for (Player p : this.getServer().getOnlinePlayers()) {
            AuthPlayer ap = AuthPlayer.getAuthPlayer(p);
            if (ap.isLoggedIn()) ap.logout(this, false);
            ap.RestoreSurvivalInventory(p); // Restore Inventory
        }

        PConfManager.saveAllManagers("Normal");
        PConfManager.purge();

        if (!Config.MySqlDbHost.equals("")) MySQL.disconnect(); // DisConnect from the Database System

        this.log.info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " Disabled!.");
    }

    public void CommandDoUpdate(String HowToDo) {
        if(HowToDo.equals("HashMap"))
            update();
        else
            updateToMySQL();
    }

    // isRegistered, forceRegister, forceLogin: API-Functions for the FastLogin Plugin
    public boolean isRegistered(String player){
        AuthPlayer ap = AuthPlayer.getAuthPlayer(player);
        if (ap == null) {
            this.getLogger().info(ChatColor.RED + ThunderAUtils.colorize(Language.ERROR_OCCURRED.toString()));
            return false;
        }
        return(ap.isRegistered());
    }

    public void forceRegister(String player, String Password) {
        AuthPlayer ap = AuthPlayer.getAuthPlayer(player);
        if (ap == null) {
            this.getLogger().info(ChatColor.RED + ThunderAUtils.colorize(Language.ERROR_OCCURRED.toString()));
            return;
        }
        if (ap.isRegistered()) {
            this.getLogger().info(ChatColor.RED + ThunderAUtils.colorize(Language.PLAYER_ALREADY_REGISTERED.toString()));
            return;
        }
        for (String disallowed : Config.disallowedPasswords) {
            if (disallowed.equals("#NoPlayerName#")) disallowed = ap.getUserName();
            if (!Password.equalsIgnoreCase(disallowed)) continue;
            this.getLogger().info(ChatColor.RED + ThunderAUtils.colorize(Language.DISALLOWED_PASSWORD.toString()));
            return;
        }
        final String name = ThunderAUtils.forceGetName(ap.getUniqueId());
        if (ap.setPassword(Password, Config.passwordHashType)) {
            if(name!=player) ap.setUserName(player); //name not set?, set it!
            this.getLogger().info(ChatColor.BLUE + String.format(ThunderAUtils.colorize(Language.REGISTERED_SUCCESSFULLY.toString()), ChatColor.GRAY + player + ChatColor.BLUE));
        }
        else
            this.getLogger().info(ChatColor.RED + String.format(ThunderAUtils.colorize(Language.COULD_NOT_REGISTER.toString()), ChatColor.GRAY + player + ChatColor.RED));
    }

    public void forceLogin(String player) {
        AuthPlayer ap = AuthPlayer.getAuthPlayer(player);
        if (ap == null) {
            this.getLogger().info(ChatColor.RED + ThunderAUtils.colorize(Language.ERROR_OCCURRED.toString()));
            return;
        }
        Player p = ap.getPlayer();
        if (p == null) {
            this.getLogger().info(ChatColor.RED + ThunderAUtils.colorize(Language.PLAYER_NOT_ONLINE.toString()));
            return;
        }
        ap.login();
        this.getLogger().info(p.getName() + " " + ThunderAUtils.colorize(Language.HAS_LOGGED_IN.toString()));
    }
}

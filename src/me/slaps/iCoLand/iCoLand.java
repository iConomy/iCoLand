/**
 * 
 */

package me.slaps.iCoLand;

import java.io.File;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Locale;

import me.slaps.iCoLand.iCoLandBlockListener;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;



/**
 * iCoLand
 * 
 * @author magik
 *
 */
public class iCoLand extends JavaPlugin {
    
    public static boolean enabled = false;

	public static String name; // = "iCoLand";
	public static String codename = "fattire";
	public static String version; // = "0.0.0001";
	
	public static File pluginDirectory;
	
	public static Server server;
	
	public static Logger logger = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile desc;
	
	public static PermissionHandler perms;
	public static iConomy ic;
	
	public static iCoLandBlockListener blockListener;
	public static iCoLandPlayerListener playerListener;
	public static iCoLandPluginListener pluginListener;
	public static iCoLandCommandListener commandListener;
	public static iCoLandEntityListener entityListener;
	
	public static LandManager landMgr;
    public static HashMap<String, String> cmdMap;
    public static HashMap<String, Cuboid> tmpCuboidMap;
    
    public static DecimalFormat df;
    
    private int taskIdHeal = -1;
    private int taskIdMob = -1;
    private int taskIdTax = -1;
    
    public iCoLand() {
        // setup command map
        cmdMap = new HashMap<String, String>();
        tmpCuboidMap = new HashMap<String, Cuboid>();
        
        Locale.setDefault(Locale.US);
        df = new DecimalFormat("#.00");
    }
    
    public static void severe(String msg) {
        logger.severe("["+name+"] "+ msg);
    }
    
    public static void info(String msg) {
    	logger.info("["+name+"] "+ msg);
    }
    
    public static void warning(String msg) {
    	logger.warning("["+name+"] "+ msg);
    }

	public void onDisable() {
        iCoLand.ic = null;
        iCoLand.perms = null;
        
        iCoLand.landMgr.close();
        iCoLand.landMgr = null;
        iCoLand.cmdMap.clear();
        iCoLand.tmpCuboidMap.clear();
        
        iCoLand.enabled = false;
        Config.loaded = false;
        
        BukkitScheduler bs= server.getScheduler();
        bs.cancelTasks(this);
        if ( bs.isCurrentlyRunning(taskIdHeal) || bs.isQueued(taskIdHeal) )
            bs.cancelTask(taskIdHeal);
        if ( bs.isCurrentlyRunning(taskIdMob) || bs.isQueued(taskIdMob) )
            bs.cancelTask(taskIdMob);
        if ( bs.isCurrentlyRunning(taskIdTax) || bs.isQueued(taskIdTax) )
            bs.cancelTask(taskIdTax);
        
        
		info("Version ["+version+"] ("+codename+") disabled");
	}

	public void onEnable() {
	    initPluginInfo();
		
		Config.getConfig(pluginDirectory);
	  	
        pluginListener = new iCoLandPluginListener(this);

        // try to check for if external plugins already enabled
	  	tryEnablePlugins();
    }
	
    public void initPluginInfo() {
        desc = getDescription();
        name = desc.getName();
        version = desc.getVersion();
        
        pluginDirectory = getDataFolder();
        //pluginDirectory = new File("./plugins/iCoLand/");

        pluginDirectory.mkdir();
        
        server = getServer();
    }

    public void tryEnablePlugins() {
        PluginManager pm = server.getPluginManager();
        Plugin plugin;
        
        plugin = pm.getPlugin("iConomy");
        if( ( plugin != null ) && plugin.isEnabled() ) {
            ic = ((iConomy)plugin);
            info("Successfully linked with iConomy");
        }
        
        plugin = pm.getPlugin("Permissions");
        if( ( plugin != null ) && plugin.isEnabled() ) {
            perms = ((Permissions)plugin).getHandler();
            info("Successfully linked with Permissions");           
        }
        
         if ( Config.loaded && ( iCoLand.ic != null && iCoLand.perms != null ) ) {
             setup();
             if ( enabled ) {
                 iCoLand.info("Version ["+iCoLand.version+"] ("+iCoLand.codename+") enabled. "+
                         iCoLand.landMgr.countLandsOwnedBy(null)+" lands found."+
                         (Config.debugMode?" **DEBUG MODE ENABLED**":""));
             }
         }
    }	
	

    
    public void setup() {
        if ( !enabled ) {
            // setup location manager
            iCoLand.info("Initializing land manager...");
            //LandDBH2 landDB = new LandDBH2(iCoLand.pluginDirectory + File.separator + Config.h2DBFile);
            LandDBMySQL landDB = new LandDBMySQL("jdbc:mysql://localhost:3306/", "iCoLand", "root", "");
            if ( !landDB.enabled ) {
                iCoLand.severe("Could not initialize land manager, not enabling plugin");
                return;
            }
                
            iCoLand.landMgr = new LandManager(landDB);

            // setup listeners
            blockListener =  new iCoLandBlockListener(this);
            playerListener = new iCoLandPlayerListener(this);
            entityListener = new iCoLandEntityListener(this);
            commandListener = new iCoLandCommandListener();
            getCommand("icl").setExecutor(commandListener);
            
            // setup events
            taskIdHeal = server.getScheduler().scheduleSyncRepeatingTask(this, new TaskLandHeal(), 20, Config.healTime*20);
            taskIdMob = server.getScheduler().scheduleSyncRepeatingTask(this, new TaskLandMobKill(this), 20, Config.mobRemovalTime*20/3);
            if ( Config.taxTimeMinutes > 0 ) 
                taskIdTax = server.getScheduler().scheduleSyncRepeatingTask(this, new TaskLandTaxes(this), 20, 10*20);

            enabled = true;
            
            //iCoLand.landMgr.test();
        }
    }
    
    public static boolean hasPermission(CommandSender sender, String permString) {
        if (sender instanceof Player)
            return hasPermission(((Player)sender).getWorld().getName(), ((Player)sender).getName(), permString);
            //return Permissions.Security.permission((Player) sender, name.toLowerCase() + "." + permString);
        return true;
    }
    
    public static boolean hasPermission(String world, String playerName, String permString) {
        OfflinePlayer player = new OfflinePlayer(server, server.getWorld(world), playerName);
        return Permissions.Security.permission((Player)player, name.toLowerCase() + "." + permString);
    }

	
	
}

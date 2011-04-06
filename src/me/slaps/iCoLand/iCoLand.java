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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
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
	public static String codename = "initial";
	public static String version; // = "0.0.0001";
	
	public static File pluginDirectory;
	
	public static Server server;
	
	public static Logger logger = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile desc;
	
	public static Permissions perms;
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
		info("Version ["+version+"] ("+codename+") disabled");
	}

	public void onEnable() {

		desc = getDescription();
		name = desc.getName();
		version = desc.getVersion();
		
		pluginDirectory = getDataFolder();

		pluginDirectory.mkdir();
		
		Config.getConfig(pluginDirectory);
		
		server = getServer();
		
		
		// setup listeners
	  	getCommand("icl").setExecutor(new iCoLandCommandListener());        
		blockListener =  new iCoLandBlockListener(this);
		playerListener = new iCoLandPlayerListener(this);
	  	pluginListener = new iCoLandPluginListener(this);
	  	entityListener = new iCoLandEntityListener(this);
	  	
	  	// try to check for if external plugins already enabled
	  	pluginListener.tryEnablePlugins();

		info("Version ["+version+"] ("+codename+") enabled" + (Config.debugMode?" **DEBUG MODE ENABLED**":""));
    }
	

    public static boolean hasPermission(CommandSender sender, String permString) {
        if (sender instanceof Player)
            return Permissions.Security.permission((Player) sender, name.toLowerCase() + "." + permString);
        return true;
    }
    
    public void setup() {
        if ( !enabled ) {
            enabled = true;
            
            server.getScheduler().scheduleSyncRepeatingTask(this, new HealTask(), 100, Config.healTime*20);
            server.getScheduler().scheduleSyncRepeatingTask(this, new MobKillTask(), 100, Config.mobRemovalTime*20);
            
            // setup location manager
            //landMgr = new LandManager((LandDB)(new LandDBFlatFile(new File(pluginDirectory + File.separator + "lands.yml"))));
            iCoLand.info("Permissions and iConomy found, initializing land manager");
            iCoLand.landMgr = new LandManager((LandDB)(new LandDBH2(iCoLand.pluginDirectory + File.separator + Config.h2DBFile)));
            //clear command list
            iCoLand.cmdMap.clear();
            
            //iCoLand.landMgr.test();
        }
    }
	
	
}

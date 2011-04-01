/**
 * 
 */

package me.slaps.iCoLand;

import java.io.File;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Locale;

import me.slaps.iCoLand.iConomyLandBlockListener;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;



/**
 * iConomyLand
 * 
 * @author magik
 *
 */
public class iCoLand extends JavaPlugin {

	public static String name; // = "iConomyLand";
	public static String codename = "initial";
	public static String version; // = "0.0.0001";
	
	public static Server server;
	
	public static Logger logger = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile desc;
	
	public static Permissions perms;
	public static iConomy ic;
	
	public static iConomyLandBlockListener blockListener;
	public static iConomyLandPlayerListener playerListener;
	public static iConomyLandPluginListener pluginListener;
	public static iConomyLandCommandListener commandListener;
	
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

		getDataFolder().mkdir();
		
		Config.getConfig(getDataFolder());
		
		server = getServer();
		
        // setup location manager
		landMgr = new LandManager((LandDB)(new LandDBFlatFile(new File(getDataFolder() + File.separator + "lands.yml"))));

        //clear command list
        cmdMap.clear();
		
		// setup listeners
	  	getCommand("icl").setExecutor(new iConomyLandCommandListener());        
		blockListener =  new iConomyLandBlockListener(this);
		playerListener = new iConomyLandPlayerListener(this);
	  	pluginListener = new iConomyLandPluginListener(this);
	  	
	  	// try to check for if external plugins already enabled
	  	pluginListener.tryEnablePlugins(getServer().getPluginManager());

		info("Version ["+version+"] ("+codename+") enabled" + (Config.debugMode?" **DEBUG MODE ENABLED**":""));
    }
	

    public static boolean hasPermission(CommandSender sender, String permString) {
        if (sender instanceof Player)
            return Permissions.Security.permission((Player) sender, name.toLowerCase() + "." + permString);
        return true;
    }
	
	
}

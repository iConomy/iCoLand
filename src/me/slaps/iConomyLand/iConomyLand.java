/**
 * 
 */
package me.slaps.iConomyLand;

import java.io.File;
import java.util.logging.Logger;
import java.util.HashMap;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

//import com.nijiko.coelho.iConomy.*;


/**
 * @author magik
 *
 */
public class iConomyLand extends JavaPlugin {

	public static String name; // = "iConomyLand";
	public static String codename = "initial";
	public static String version; // = "0.0.0001";
	
	public static boolean debugMode = false;
	
	public static Logger logger = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile desc;
	
	public static Permissions perms;
	public static iConomy ic;
	
	public static iConomyLandBlockListener blockListener;
	public static iConomyLandPlayerListener playerListener;
	public static iConomyLandPluginListener pluginListener;
	public static iConomyLandCommandListener commandListener;
	
	protected static LandManager landMgr;
	//protected static HashMap<String, ShopLocation> tmpShop = new HashMap<String, ShopLocation>();
    protected static HashMap<String, String> cmdMap = new HashMap<String, String>();

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
		//directory = getDataFolder() + File.separator;
		
		getConfig();
		
        // setup location manager
		landMgr = new LandManager(this);
        
        //clear command list
        cmdMap.clear();
		
		// setup listeners
	  	getCommand("icl").setExecutor(new iConomyLandCommandListener());        
		blockListener =  new iConomyLandBlockListener(this);
		playerListener = new iConomyLandPlayerListener(this);
	  	pluginListener = new iConomyLandPluginListener(this);
	  	
	  	// try to check for if external plugins already enabled
	  	pluginListener.tryEnablePlugins(getServer().getPluginManager());

		info("Version ["+version+"] ("+codename+") enabled" + (iConomyLand.debugMode?" **DEBUG MODE ENABLED**":""));
    }
    
	private void getConfig() {
	    File configFile = new File(getDataFolder() + File.separator + "config.yml");
	    
	    if ( !configFile.exists() ) {
	        saveConfig(configFile);
	    }
	    
	    loadConfig(configFile);
	}
	    
	private void loadConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();
        iConomyLand.debugMode = config.getBoolean("debug", false);
	}
	
	private void saveConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.setProperty("debug", iConomyLand.debugMode);
        config.save();
	}
	
	public static boolean hasPermission(CommandSender sender, String permString)
	{
		if (sender instanceof Player)
			return Permissions.Security.permission((Player)sender, name.toLowerCase()+"."+permString);
		return true;
	}	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	    //return playerListener.onCommand( sender, cmd, commandLabel, args);
		return false;
	}
	
	
	
}

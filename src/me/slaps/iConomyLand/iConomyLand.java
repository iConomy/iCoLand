/**
 * 
 */
package me.slaps.DMWrapper;

import java.io.File;
import java.util.logging.Logger;
import java.util.HashMap;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
import com.nijikokun.bukkit.Permissions.Permissions;


/**
 * @author magik
 *
 */
public class DMWrapper extends JavaPlugin {

	public static String name; // = "DMWrapper";
	public static String codename = "Botswana";
	public static String version; // = "0.03";
	
	public static Boolean debugMode = false;
	
	public static Logger logger = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile desc;
	
	public static Permissions perms;
	public static DynamicMarket dm;
	
	public static DMWrapperBlockListener blockListener;
	public static DMWrapperPlayerListener playerListener;
	public static DMWrapperPluginListener pluginListener;
	
	protected static LocationManager locMgr;
	protected static HashMap<String, ShopLocation> tmpShop = new HashMap<String, ShopLocation>();
    protected static HashMap<String, String> cmdMap = new HashMap<String, String>();

    public static void info(String msg) {
    	logger.info("["+name+"] "+ msg);
    }
    public static void warning(String msg) {
    	logger.warning("["+name+"] "+ msg);
    }

	@Override
	public void onDisable() {
		info("Version ["+version+"] ("+codename+") disabled");
	}

	@Override
    public void onEnable() {
		desc = getDescription();
		name = desc.getName();
		version = desc.getVersion();

		getDataFolder().mkdir();
		//directory = getDataFolder() + File.separator;
		
		getConfig();
		
        // setup location manager
        locMgr = new LocationManager(this);
        
        //clear command list
        cmdMap.clear();
		
		// setup listeners
		blockListener =  new DMWrapperBlockListener(this);
		playerListener = new DMWrapperPlayerListener(this);
	  	pluginListener = new DMWrapperPluginListener(this);
	  	
	  	// try to check for if external plugins already enabled
	  	pluginListener.tryEnablePlugins(getServer().getPluginManager());

		info("Version ["+version+"] ("+codename+") enabled" + (DMWrapper.debugMode?" **DEBUG MODE ENABLED**":""));
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
        DMWrapper.debugMode = config.getBoolean("debug", false);
	}
	
	private void saveConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.setProperty("debug", DMWrapper.debugMode);
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
	    return playerListener.onCommand( sender, cmd, commandLabel, args);
	}
	
	
	
}

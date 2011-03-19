package me.slaps.iConomyLand;

import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;


import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

public class iConomyLandPluginListener extends ServerListener {
	
    iConomyLand parent;
    
	public iConomyLandPluginListener(iConomyLand plug) {
	    parent = plug;
	    plug.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, this, Priority.Monitor, plug);
	}
	
	@Override
    public void onPluginEnabled(PluginEvent event) {
    	if ( event.getPlugin().getDescription().getName().equals("iConomy") ) {
            Plugin pluginIC = parent.getServer().getPluginManager().getPlugin("iConomy");
	 
            if ( pluginIC != null )	enableiConomy((iConomy)pluginIC);
    	}

    	if ( event.getPlugin().getDescription().getName().equals("Permissions") ) {
        	Plugin pluginPerms = parent.getServer().getPluginManager().getPlugin("Permissions");
        	
	        if ( pluginPerms != null )	enablePermissions((Permissions)pluginPerms);
    	}
    }
	
	public void enableiConomy(iConomy plugin) {
		iConomyLand.ic = plugin;
  		iConomyLand.info("Successfully linked with iConomy");
	}
	
	public void enablePermissions(Permissions plugin) {
		iConomyLand.perms = plugin;
		iConomyLand.info("Successfully linked with Permissions");	  		
	}
	
	public void tryEnablePlugins(PluginManager pm) {
	  	if( pm.getPlugin("iConomy").isEnabled() ) {
	  		Plugin plugin = pm.getPlugin("iConomy");
	  		enableiConomy((iConomy)plugin);
	  	}
	  	if( pm.getPlugin("Permissions").isEnabled() ) {
	  		Plugin plugin = pm.getPlugin("Permissions");
	  		enablePermissions((Permissions)plugin);
	  	}
	}	
}
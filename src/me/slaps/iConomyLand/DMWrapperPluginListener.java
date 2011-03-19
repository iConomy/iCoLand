package me.slaps.DMWrapper;

import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;


import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
import com.nijikokun.bukkit.Permissions.Permissions;

public class DMWrapperPluginListener extends ServerListener {
	
    DMWrapper parent;
    
	public DMWrapperPluginListener(DMWrapper plug) {
	    parent = plug;
	    plug.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, this, Priority.Monitor, plug);
	}
	
	@Override
    public void onPluginEnabled(PluginEvent event) {
    	if(event.getPlugin().getDescription().getName().equals("DynamicMarket")) {
            Plugin pluginDM = DynamicMarket.getTheServer().getPluginManager().getPlugin("DynamicMarket");
	 
            if (pluginDM != null)	enableDynamicMarket((DynamicMarket)pluginDM);
    	}

    	if(event.getPlugin().getDescription().getName().equals("Permissions")) {
        	Plugin pluginPerms = DynamicMarket.getTheServer().getPluginManager().getPlugin("Permissions");
        	
	        if(pluginPerms != null)	enablePermissions((Permissions)pluginPerms);
    	}
    }
	
	public void enableDynamicMarket(DynamicMarket plugin) {
		DMWrapper.dm = plugin;
		DMWrapper.dm.hookWrapper(parent);
  		DMWrapper.info("Successfully linked with DynamicMarket");
	}
	
	public void enablePermissions(Permissions plugin) {
		DMWrapper.perms = plugin;
		DMWrapper.info("Successfully linked with Permissions");	  		
	}
	
	public void tryEnablePlugins(PluginManager pm) {
	  	if(pm.getPlugin("DynamicMarket").isEnabled() && DMWrapper.dm == null) {
	  		Plugin plugin = pm.getPlugin("DynamicMarket");
	  		enableDynamicMarket((DynamicMarket)plugin);
	  	}
	  	if(pm.getPlugin("Permissions").isEnabled() && DMWrapper.perms == null) {
	  		Plugin plugin = pm.getPlugin("Permissions");
	  		enablePermissions((Permissions)plugin);
	  	}
	}	
}
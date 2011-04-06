package me.slaps.iCoLand;

import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.Event.Priority;

public class iCoLandPluginListener extends ServerListener {
	
    iCoLand parent;
    
	public iCoLandPluginListener(iCoLand plug) {
	    parent = plug;
        plug.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, this, Priority.Monitor, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, this, Priority.Monitor, plug);
	}
	
	@Override
    public void onPluginEnable(PluginEnableEvent event) {
    	if ( Misc.isAny(event.getPlugin().getDescription().getName(), "iConomy", "Permissions" ) ) {
            parent.tryEnablePlugins();
    	}
    }

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
	    if ( event.getPlugin().getDescription().getName().equals("iConomy") ) {
	        iCoLand.ic = null;
	    }
	    
	    if ( event.getPlugin().getDescription().getName().equals("Permissions") )
	        iCoLand.perms = null;
	}
	


	

}
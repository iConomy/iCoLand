package me.slaps.iCoLand;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;

public class iCoLandEntityListener extends EntityListener {
    
    public iCoLandEntityListener(iCoLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Priority.Low, plug);
    }
    
    public void onCreatureSpawn (CreatureSpawnEvent event) {
        Integer id = iCoLand.landMgr.getLandId(event.getEntity().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                event.setCancelled(true);
            }
        }
    }
}

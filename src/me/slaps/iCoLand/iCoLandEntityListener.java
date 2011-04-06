package me.slaps.iCoLand;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class iCoLandEntityListener extends EntityListener {
    
    public iCoLandEntityListener(iCoLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, this, Priority.Low, plug);
//        plug.getServer().getPluginManager().registerEvent(Event.Type.EXPLOSION_PRIME, this, Priority.Low, plug);
    }
    
    public void onCreatureSpawn (CreatureSpawnEvent event) {
        Integer id = iCoLand.landMgr.getLandId(event.getEntity().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onEntityExplode ( EntityExplodeEvent event ) {
        Integer id = iCoLand.landMgr.getLandId(event.getEntity().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("noboom") ) {
                event.setCancelled(true);
            }
        } else if ( !Config.unclaimedLandCanBoom ) {
            event.setCancelled(true);
        }
    }
    
    public void onExplosionPrime ( ExplosionPrimeEvent event ) {
        Integer id = iCoLand.landMgr.getLandId(event.getEntity().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("noboom") ) {
                event.setCancelled(true);
            }
        } else if ( !Config.unclaimedLandCanBoom ) {
            event.setCancelled(true);
        }
    }
    
}

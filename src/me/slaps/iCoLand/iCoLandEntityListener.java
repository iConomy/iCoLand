package me.slaps.iCoLand;

import java.util.ArrayList;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class iCoLandEntityListener extends EntityListener {
    
    public iCoLandEntityListener(iCoLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, this, Priority.Low, plug);
//        plug.getServer().getPluginManager().registerEvent(Event.Type.EXPLOSION_PRIME, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, this, Priority.Low, plug);
    }
    
    public void onCreatureSpawn (CreatureSpawnEvent event) {
        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getEntity().getLocation());
        for(Integer id : ids) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onEntityDamage ( EntityDamageEvent event ) {
        DamageCause cause = event.getCause();

        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getEntity().getLocation());

        if ( cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) ) {
            for(Integer id : ids) {
                Land land = iCoLand.landMgr.getLandById(id);
                if ( land.hasAddon("nofire") ) {
                    event.setCancelled(true);
                }
            }
            
            if ( ids.size() == 0 && !Config.unclaimedLandCanBurn ) {
                event.setCancelled(true);
            }
        } else if ( cause.equals(DamageCause.BLOCK_EXPLOSION) || cause.equals(DamageCause.ENTITY_EXPLOSION) ) {
            for(Integer id : ids) {
                Land land = iCoLand.landMgr.getLandById(id);
                if ( land.hasAddon("noboom") ) {
                    event.setCancelled(true);
                }
            } 
            
            if ( ids.size() == 0 && !Config.unclaimedLandCanBoom ) {
                event.setCancelled(true);
            }
            
        }
    }
    
    public void onEntityExplode ( EntityExplodeEvent event ) {
        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getEntity().getLocation());
        for(Integer id : ids) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("noboom") ) {
                event.setCancelled(true);
            }
        } 

        if ( ids.size() == 0 && !Config.unclaimedLandCanBoom ) {
            event.setCancelled(true);
        }
    }
    
    public void onExplosionPrime ( ExplosionPrimeEvent event ) {
        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getEntity().getLocation());
        for(Integer id : ids) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("noboom") ) {
                event.setCancelled(true);
            }
        }

        if ( ids.size() == 0 && !Config.unclaimedLandCanBoom ) {
            event.setCancelled(true);
        }
    }
    
}

package me.slaps.iCoLand;

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
        Integer id = iCoLand.landMgr.getLandId(event.getEntity().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onEntityDamage ( EntityDamageEvent event ) {
        DamageCause cause = event.getCause();

        Integer id = iCoLand.landMgr.getLandId(event.getEntity().getLocation());

        if ( cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) ) {
            if ( id > 0 ) {
                Land land = iCoLand.landMgr.getLandById(id);
                if ( land.hasAddon("nofire") ) {
                    event.setCancelled(true);
                }
            } else if ( !Config.unclaimedLandCanBurn ) {
                event.setCancelled(true);
            }
        } else if ( cause.equals(DamageCause.BLOCK_EXPLOSION) || cause.equals(DamageCause.ENTITY_EXPLOSION) ) {
            if ( id > 0 ) {
                Land land = iCoLand.landMgr.getLandById(id);
                if ( land.hasAddon("noboom") ) {
                    event.setCancelled(true);
                }
            } else if ( !Config.unclaimedLandCanBoom ) {
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

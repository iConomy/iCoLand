package me.slaps.iCoLand;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MobKillTask implements Runnable {
    
    public void run() {
        List<World> worlds = iCoLand.server.getWorlds();
        for(World world : worlds) {
            List<LivingEntity> entities = world.getLivingEntities();
            for(LivingEntity entity : entities) {
                if ( !(entity instanceof Player) ) {
                    Integer id = iCoLand.landMgr.getLandId(entity.getLocation());
                    if ( id > 0 ) {
                        if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                            entity.remove();
                        }
                    }
                }
            }
        }
        
        
        
    }
}

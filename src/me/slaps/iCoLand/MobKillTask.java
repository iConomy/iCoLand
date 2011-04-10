package me.slaps.iCoLand;

import java.util.ArrayList;
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
                    ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(entity.getLocation());
                    for(Integer id : ids) {
                        if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                            entity.remove();
                        }
                    }
                }
            }
        }
        
        
        
    }
}

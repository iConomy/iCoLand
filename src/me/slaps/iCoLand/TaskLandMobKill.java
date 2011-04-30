package me.slaps.iCoLand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TaskLandMobKill implements Runnable {

    iCoLand ic;
    boolean main;
    List<LivingEntity> entities;
    
    public TaskLandMobKill(boolean main, iCoLand ic) {
        this.ic = ic;
        this.main = true;
    }
    
    public TaskLandMobKill(boolean main, iCoLand ic, List<LivingEntity> entities) {
        this.ic = ic;
        this.main = main;
        this.entities = entities;
    }
    
    public void run() {
        if ( iCoLand.landMgr == null ) return;
        if ( main ) {
            List<World> worlds = iCoLand.server.getWorlds();
            for(World world : worlds) {
                List<LivingEntity> entities = world.getLivingEntities();
                int jobSize = 20;
                for(int i=0;i<entities.size()/jobSize+1;i++) {
                    iCoLand.server.getScheduler().scheduleSyncDelayedTask((Plugin)ic, new TaskLandMobKill(false, ic, entities.subList(0, jobSize*i)), i+1);
                }
            }
        } else {
            for(LivingEntity entity : entities) {
                if ( !(entity instanceof Player) ) {
                    if ( entity.getLocation() != null ) {
                        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(entity.getLocation());
                        for(Integer id : ids) {
                            if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                                if ( iCoLand.landMgr.preventSpawn(id, entity) ) {
                                    entity.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
        
    }
}

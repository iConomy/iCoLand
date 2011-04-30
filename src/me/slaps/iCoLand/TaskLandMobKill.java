package me.slaps.iCoLand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TaskLandMobKill implements Runnable {

    iCoLand ic;
    private static HashMap<Integer, Long> timeMap;
    private ArrayList<LivingEntity> checkList;
    boolean main;
    private static ArrayList<Integer> taskIDs;

    public TaskLandMobKill(iCoLand ic) {
        this.ic = ic;
        this.main = true;
        if ( timeMap == null ) {
            timeMap = new HashMap<Integer,Long>();
            taskIDs = new ArrayList<Integer>();
        }
    }

    public TaskLandMobKill(iCoLand ic, ArrayList<LivingEntity> check) {
        this(ic);
        this.main = false;
        this.checkList = check;
    }
    
    public static boolean checkNow(int id) {
        if (!timeMap.containsKey(id)) {
            return true;
        } else if ( System.currentTimeMillis() > timeMap.get(id) ) {
            return true;
        } else {
            return false;
        }
    }
    
    
    public void run() {
        if ( main ) {
            if ( iCoLand.landMgr == null ) return;
            
            if ( checkNow(-1) ) {
                // clean up old ids every 30 seconds
                timeMap.put(-1, System.currentTimeMillis() + 30000);
                ArrayList<Integer> list = new ArrayList<Integer>();
                
                long thresh = System.currentTimeMillis() - 30000;
                int removed = 0;
                for( Integer entity : timeMap.keySet() )
                    if ( entity >= 0 && timeMap.get(entity) < thresh )  {
                        list.add(entity);
                        removed++;
                    }
                for( Integer entity : list )
                    timeMap.remove(entity);
                iCoLand.info("Removed "+removed+" old entity ids");
            }
            
            while( taskIDs.size() > 0 && 
                    !( iCoLand.server.getScheduler().isCurrentlyRunning(taskIDs.get(0)) || iCoLand.server.getScheduler().isQueued(taskIDs.get(0)) ) ) {
                taskIDs.remove(0);
            }
                
            if ( taskIDs.size() > 0 ) return;
                    
            List<World> worlds = iCoLand.server.getWorlds();
            int i = 0;
            int j = 0;
            int k = 0;
            int jobSize = 2;
            ArrayList<LivingEntity> checkList = new ArrayList<LivingEntity>();
            for(World world : worlds) {
                List<LivingEntity> entities = world.getLivingEntities();
                for(LivingEntity entity : entities) {
                    if ( !(entity instanceof Player) ) {
                        int eid = entity.getEntityId();
                        if ( checkNow(eid) ) {
                            checkList.add(entity);
                            j++;
                            k++;
                        }
                    }
                    if ( j >= jobSize ) {
                        taskIDs.add(iCoLand.server.getScheduler().scheduleSyncDelayedTask(ic, new TaskLandMobKill(ic, checkList), i));
                        checkList = new ArrayList<LivingEntity>();
                        i++;
                        j = 0;
                    }
                }
                if ( checkList.size() > 0 ) {
                    taskIDs.add(iCoLand.server.getScheduler().scheduleSyncDelayedTask(ic, new TaskLandMobKill(ic, checkList), i));
                    checkList = new ArrayList<LivingEntity>();
                    i++;
                }
                if ( Config.debugMode1 ) iCoLand.info("mob kill task, checking "+k+"/"+entities.size()+" mobs in "+i+" tasks in world "+world.getName());
            }
            
        } else {
            for( LivingEntity entity : checkList ) {
                if ( entity.getLocation() != null ) {
                    int eid = entity.getEntityId();
//                            long before = System.currentTimeMillis();
                    ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(entity.getLocation());
//                            iCoLand.info("query took: "+(System.currentTimeMillis()-before));
                    for(Integer id : ids) {
                        if ( iCoLand.landMgr.getLandById(id).hasAddon("nospawn") ) {
                            if ( !iCoLand.landMgr.preventSpawn(id, entity) ) {
                                TaskLandMobKill.timeMap.remove(eid);
                                entity.remove();
                            }
                        }
                    }
                    long now = System.currentTimeMillis();
                    TaskLandMobKill.timeMap.put(eid, now + Config.mobRemovalTime*1000 );
                }
            }
        }

        
    }
}

package me.slaps.iCoLand;

import org.bukkit.Location;

public class TaskSetBlock implements Runnable {
    public Location loc;
    public int setId;
    
    TaskSetBlock(Location loc, int setId) { 
        this.loc = loc; 
        this.setId = setId; 
    } 
    
    public void run() { 
        if ( loc != null && loc.getWorld() != null && loc.getWorld().getBlockAt(loc) != null )
            loc.getWorld().getBlockAt(loc).setTypeId(setId);
    }
    
}

package me.slaps.iCoLand;

import org.bukkit.Location;

public class BlockLocation {
    public int x, y, z;
    public String world;
    
    public BlockLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public boolean equals(Object obj) {
        if ( obj instanceof BlockLocation ) {
            BlockLocation other = (BlockLocation)obj;
            
            return ( other.world.equals(world) && x == other.x && y == other.y && z == other.z );
        } else 
            return false;
    }
    
    public String toString() {
        return "x:"+x+",y:"+y+",z:"+z;
    }
    
    public int hashCode() {
        return x + y << 8 + z << 16 + world.hashCode() << 24; 
    }
    
}

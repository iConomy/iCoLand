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
    
    
}

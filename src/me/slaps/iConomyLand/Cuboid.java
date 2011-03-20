package me.slaps.iConomyLand;

import org.bukkit.Location;

public class Cuboid {
	public Location setLoc1;
	public Location setLoc2;
	private Location LocMin;
	private Location LocMax;
	private boolean valid = false;
	
	public Cuboid() {
		
	}
	
	public Cuboid(Cuboid other) {
	    setLoc1 = other.setLoc1;
	    setLoc2 = other.setLoc2;
	    computeNewBlock();
	}
	
	public Cuboid(Location loc) {
		setLoc1 = loc;
	}
	
	public Cuboid(Location loc1, Location loc2) {
		setLoc1 = loc1;
		setLoc2 = loc2;
		computeNewBlock();
	}
	
	public boolean setLocation(Location loc) {
		if ( setLoc1 == null ) {
		    setLoc1 = loc;
	        return computeNewBlock();
		} else if ( setLoc2 == null ) {
		    setLoc2 = loc;
	        return computeNewBlock();
		} else {
		    return false;
		}		
	}
	
	public boolean isIn(Location loc) {
		if (( LocMin == null ) && (LocMax == null)) return false;
        if ( !loc.getWorld().equals(LocMin.getWorld()) ) return false;
		return 
		   ( (loc.getBlockX() >= LocMin.getBlockX()) && 
		     (loc.getBlockX() <= LocMax.getBlockX()) &&
			 (loc.getBlockY() >= LocMin.getBlockY()) && 
			 (loc.getBlockY() <= LocMax.getBlockY()) &&
			 (loc.getBlockZ() >= LocMin.getBlockZ()) && 
			 (loc.getBlockZ() <= LocMax.getBlockZ()) );
	}
	
	public boolean intersects(Cuboid other) {
		double maxX = other.LocMax.getBlockX();
		double maxY = other.LocMax.getBlockX();
		double maxZ = other.LocMax.getBlockZ();
		double minX = other.LocMin.getBlockX();
		double minY = other.LocMin.getBlockY();
		double minZ = other.LocMin.getBlockZ();
		if ( isIn(new Location(other.setLoc1.getWorld(), maxX, maxY, maxZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), maxX, maxY, minZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), maxX, minY, maxZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), maxX, minY, minZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), minX, maxY, maxZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), minX, maxY, minZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), minX, minY, maxZ)) ) return true;
		if ( isIn(new Location(other.setLoc1.getWorld(), minX, minY, minZ)) ) return true;
		
		maxX = LocMax.getBlockX();
		maxY = LocMax.getBlockX();
		maxZ = LocMax.getBlockZ();
		minX = LocMin.getBlockX();
		minY = LocMin.getBlockY();
		minZ = LocMin.getBlockZ();
		if ( other.isIn(new Location(setLoc1.getWorld(), maxX, maxY, maxZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), maxX, maxY, minZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), maxX, minY, maxZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), maxX, minY, minZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), minX, maxY, maxZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), minX, maxY, minZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), minX, minY, maxZ)) ) return true;
		if ( other.isIn(new Location(setLoc1.getWorld(), minX, minY, minZ)) ) return true;
		
		return false;
	}
	
	
	private boolean computeNewBlock() {
		if ( ( setLoc1 == null ) || ( setLoc2 == null ) ) {
			return false;
		} else {
		    if ( setLoc1.getWorld().equals(setLoc2.getWorld()) ) {
        		double maxX = Math.max(setLoc1.getBlockX(),setLoc2.getBlockX());
        		double maxY = Math.max(setLoc1.getBlockY(),setLoc2.getBlockY());
        		double maxZ = Math.max(setLoc1.getBlockZ(),setLoc2.getBlockZ());
        		double minX = Math.min(setLoc1.getBlockX(),setLoc2.getBlockX());
        		double minY = Math.min(setLoc1.getBlockY(),setLoc2.getBlockY());
        		double minZ = Math.min(setLoc1.getBlockZ(),setLoc2.getBlockZ());
        		
        		LocMin = new Location(setLoc1.getWorld(), minX, minY, minZ);
        		LocMax = new Location(setLoc1.getWorld(), maxX, maxY, maxZ);
        		
        		valid = true;
        		return true;
		    } else {
		        return false;
		    }
		}
	}
	
	public boolean isValid() {
	    return valid;
	}
	
}

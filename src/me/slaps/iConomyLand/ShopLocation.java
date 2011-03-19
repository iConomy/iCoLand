package me.slaps.DMWrapper;

import org.bukkit.Location;

public class ShopLocation {
	public boolean set = false;
	
	Integer id;
	
	public Location setLoc1;
	public Location setLoc2;
	private Location LocMin;
	private Location LocMax;
	
	public ShopLocation() {
		
	}
	
	public ShopLocation(Location loc) {
		setLoc1 = loc;
	}
	
	public ShopLocation(Integer id, Location loc1, Location loc2) {
		this.id = id;
		setLoc1 = loc1;
		setLoc2 = loc2;
		computeNewBlock();
	}
	
	public boolean setLocation(Integer i, Location loc) {
		if ( i == 1 ) {
		    setLoc1 = loc;
		} else if ( i == 2 ) {
		    setLoc2 = loc;
		}
		
		return computeNewBlock();
	}
	
	public boolean isInShop(Location loc) {
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
	
	public boolean intersectsShop(ShopLocation other) {
		double maxX = other.LocMax.getBlockX();
		double maxY = other.LocMax.getBlockX();
		double maxZ = other.LocMax.getBlockZ();
		double minX = other.LocMin.getBlockX();
		double minY = other.LocMin.getBlockY();
		double minZ = other.LocMin.getBlockZ();
		if ( isInShop(new Location(other.setLoc1.getWorld(), maxX, maxY, maxZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), maxX, maxY, minZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), maxX, minY, maxZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), maxX, minY, minZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), minX, maxY, maxZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), minX, maxY, minZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), minX, minY, maxZ)) ) return true;
		if ( isInShop(new Location(other.setLoc1.getWorld(), minX, minY, minZ)) ) return true;
		
		maxX = LocMax.getBlockX();
		maxY = LocMax.getBlockX();
		maxZ = LocMax.getBlockZ();
		minX = LocMin.getBlockX();
		minY = LocMin.getBlockY();
		minZ = LocMin.getBlockZ();
		if ( other.isInShop(new Location(setLoc1.getWorld(), maxX, maxY, maxZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), maxX, maxY, minZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), maxX, minY, maxZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), maxX, minY, minZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), minX, maxY, maxZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), minX, maxY, minZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), minX, minY, maxZ)) ) return true;
		if ( other.isInShop(new Location(setLoc1.getWorld(), minX, minY, minZ)) ) return true;
		
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
        		
        		// make it at least player height
        		if ( maxY - minY < 2) maxY += 2;
        		
        		LocMin = new Location(setLoc1.getWorld(), minX, minY, minZ);
        		LocMax = new Location(setLoc1.getWorld(), maxX, maxY, maxZ);
        		
        		set = true;
        		return true;
		    } else {
		        return false;
		    }
		}
	}
	
}

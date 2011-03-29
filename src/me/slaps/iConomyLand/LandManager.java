package me.slaps.iConomyLand;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandManager {
	
    private LandDB landDB;
	
	public LandManager(LandDB db) {
	    landDB = db;
	}
	
	public void save() {
	    landDB.save();
	}
	
	public void load() {
	    landDB.load();
	}
	
	public boolean addLand(Cuboid sl, String owner, String perms, String addons) {
	    if ( !sl.isValid() ) return false;
		if ( intersectsExistingLand(sl) ) return false;
		Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Integer id = getNextID();
		landDB.lands.put( id, new Land(id, sl, owner, "", Land.parsePermTags(perms), Land.parseAddonTags(addons), 
		        now.toString(), now.toString()) );
		landDB.save();
		return true;
	}
	
	public boolean removeLandById(Integer id) {
	    Land land = landDB.lands.remove(id);
	    if ( land != null ) {
	        landDB.save();
	        return true;
	    } else {
	        return false;
	    }
	}
	
	public String listLand() {
		String out = "";
		Iterator<Land> itr = landDB.lands.values().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			out += tmp.getID();
			out += itr.hasNext() ? ", " : "";
		}
		return out;
	}
	
	public Location getCenterOfLand(Integer id) {
		Iterator<Land> itr = landDB.lands.values().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( id.equals(tmp.getID()) ) {
				Location dest = new Location(tmp.location.setLoc1.getWorld(), tmp.location.setLoc1.getBlockX(), 
				                             tmp.location.setLoc1.getBlockY(), tmp.location.setLoc1.getBlockZ() );
				dest.setY(dest.getY()+1);
				return dest;
			}
		}
		return null;
	}
	
	public Integer getNextID() {
		Integer i = 0;
		Iterator<Land> itr = landDB.lands.values().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.getID() > i ) i = tmp.getID();
		}
		return i+1;
	}
	
	public boolean isOwner(String playerName, Integer id) {
	    return ( playerName.equals(landDB.lands.get(id).owner) );
	}
	
	public boolean canBuild(String playerName, Location loc) {
	    Integer id = getLandId(loc);
	    if ( id > 0 ) return getLandById(id).hasPermission(playerName);
	    else          return true;
	}
	
	public boolean canBuildDestroy(String playerName, Location loc) {
	    Integer id = getLandId(loc);
	    if ( id > 0 ) {
	        Land land = landDB.lands.get(id);
	        return land.hasPermission(playerName);
	    } else { 
	        return true;
	    }
	}
	
	public boolean inLand(Location loc) {
		Iterator<Land> itr = landDB.lands.values().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.contains(loc) ) return true;
		}
		return false;
	}
	
	
	public Integer getLandId(Location loc) {
		Iterator<Land> itr = landDB.lands.values().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.contains(loc) ) return tmp.getID();
		}
		return 0;
	}
	
	public Collection<Land> getAllLands() {
	    return landDB.lands.values();
	}
	
	public Collection<Land> getLandsOwnedBy(String playerName) {
	    ArrayList<Land> ret = new ArrayList<Land>();
	    Iterator<Land> itr = landDB.lands.values().iterator();
	    while(itr.hasNext()) {
	        Land tmp = itr.next();
	        if (tmp.owner.equals(playerName)) 
	            ret.add(tmp);
	    }
	    return (Collection<Land>)ret;
	}
	
	public Land getLandById(Integer id) {
	    return landDB.lands.get(id);
	}
	
	public boolean landIdExists(Integer id) {
	    return landDB.lands.containsKey(id);
	}
	
	public boolean intersectsExistingLand(Cuboid loc) {
		Iterator<Land> itr = landDB.lands.values().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.intersects(loc) ) return true;
		}
		return false;
	}
	
	public Integer intersectsExistingLandID(Cuboid loc) {
        Iterator<Land> itr = landDB.lands.values().iterator();
        while(itr.hasNext()) {
            Land tmp = itr.next();
            if ( tmp.intersects(loc) ) return tmp.getID();
        }
        return 0;
	}
	
	public void showSelectLandInfo(CommandSender sender, Cuboid select) {
	    Messaging mess = new Messaging(sender);
	    Integer id = iConomyLand.landMgr.intersectsExistingLandID(select);
	    
	    if ( id > 0 && iConomyLand.landMgr.getLandById(id).location.equals(select) ) {
	        showExistingLandInfo(sender, landDB.lands.get(id));
	    } else if ( id > 0 ) {
            mess.send("{ERR}Intersects existing land ID# "+id);
            mess.send("{ERR}Selecting/showing land ID# "+id+" instead");
            iConomyLand.tmpCuboidMap.put(((Player)sender).getName(), iConomyLand.landMgr.getLandById(id).location );
            showExistingLandInfo(sender, landDB.lands.get(id));
	    } else {
            mess.send("{}"+Misc.headerify("{PRM}Unclaimed Land{}"));
            mess.send("Dimensoins: " + select.toDimString() );
            mess.send("Volume: " + select.volume() );
            mess.send("Price: " + iConomyLand.df.format(getPrice(select)));
	    }
	    
	}
	
	public void showSelectLandInfo(CommandSender sender, Integer id) {
	    showExistingLandInfo(sender, landDB.lands.get(id));
	}
	
	public void showExistingLandInfo(CommandSender sender, Land land) {
	    Messaging mess = new Messaging(sender);
	    mess.send("{}"+Misc.headerify("{}Land ID# {PRM}"+land.getID()+"{} -- Coords: {PRM}"+land.location.toCenterCoords()+"{}"));
        mess.send("{CMD}Owner: {}"+land.owner);
        if ( !(sender instanceof Player) || land.owner.equals(((Player)sender).getName()) ) {
            if ( !land.locationName.isEmpty() )
                mess.send("{CMD}Name: {}"+land.locationName);
            mess.send("{CMD}Created: {}"+land.dateCreated);
            mess.send("{CMD}Taxed: {}"+land.dateTaxed);
            mess.send("{CMD}Perms: {}"+Land.writePermTags(land.canBuildDestroy));            
            mess.send("{CMD}Addons: {}"+Land.writeAddonTags(land.addons));
            mess.send("{CMD}Addon Prices: {}"+Land.writeAddonPrices(land));
        }
	}
	
	public double getPrice(Cuboid target) {
	    double sum = 0;
	    Integer sx = target.LocMax.getBlockX()-target.LocMin.getBlockX()+1;
	    Integer sy = target.LocMax.getBlockY()-target.LocMin.getBlockY()+1;
	    Integer sz = target.LocMax.getBlockZ()-target.LocMin.getBlockZ()+1;
	    for(int x=0;x<sx;x++) {
	        for(int y=0;y<sy;y++) {
	            for(int z=0;z<sz;z++) {
	                sum += getPriceOfBlock(new Location(target.setLoc1.getWorld(), 
	                        target.LocMin.getBlockX()+x, target.LocMin.getBlockY()+y, target.LocMin.getBlockZ()+z));
	            }
	        }
	    }
	    return sum;
	}
	
	public double getPriceOfBlock(Location target) {
	    return iConomyLand.pricePerBlockRaw;
	}
	
	
}

package me.slaps.iCoLand;

import java.sql.Timestamp;
import java.util.Calendar;
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
	    //landDB.save();
	}
	
	public void load() {
	    //landDB.load();
	}
	
	public boolean addLand(Cuboid sl, String owner, String perms, String addons) {
	    if ( !sl.isValid() ) return false;
		if ( intersectsExistingLand(sl) > 0 ) return false;
		Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());
		
		return landDB.createNewLand(new Land(0, sl, owner, "", Land.parsePermTags(perms), 
		        Land.parseAddonTags(addons), now.toString(), now.toString()));
	}
	
	public boolean removeLandById(Integer id) {
	    return landDB.removeLandById(id);
	}
	
	public String listLand() {
		String out = "";
		Iterator<Land> itr = landDB.listAllLand().iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			out += tmp.getID();
			out += itr.hasNext() ? ", " : "";
		}
		return out;
	}
	
	public Location getCenterOfLand(Integer id) {
	    Land land = landDB.getLandById(id);
	    return land.getCenter();
	}
		
	public boolean isOwner(String playerName, Integer id) {
	    return landDB.getLandOwner(id).equals(playerName);
	}
	
	public boolean canBuildDestroy(Player player, Location loc) {

	    String playerName = player.getName();
	    Integer id = landDB.getLandId(loc);
	    if ( id > 0 ) {
	        return landDB.hasPermission(id, playerName);
	    } else {
	        if ( Config.preventGlobalBuildWithoutPerm ) {
	            return iCoLand.hasPermission(player, "canbuild");
	        } else {
	            return true;
	        }
	    }
	}
	
	public boolean inLand(Location loc) {
	    int id = landDB.getLandId(loc);
	    return (id>0)?true:false;
	}
	
	public Integer getLandId(Location loc) {
	    return landDB.getLandId(loc);
	}
	
	public ArrayList<Land> getAllLands() {
	    return landDB.listAllLand();
	}
	
	public ArrayList<Land> getLandsOwnedBy(String playerName) {
	    return landDB.listLandOwnedBy(playerName);
	}
	
	public Land getLandById(Integer id) {
	    return landDB.getLandById(id);
	}
	
	public boolean landIdExists(Integer id) {
	    return landDB.landIdExists(id);
	}
	
	public int intersectsExistingLand(Cuboid loc) {
	    return landDB.intersectsExistingLand(loc);
	}
	
	public void showSelectLandInfo(CommandSender sender, Cuboid select) {
	    Messaging mess = new Messaging(sender);
	    Integer id = intersectsExistingLand(select);
	    
	    if ( id > 0 && iCoLand.landMgr.getLandById(id).location.equals(select) ) {
	        showExistingLandInfo(sender, landDB.getLandById(id));
	    } else if ( id > 0 ) {
            mess.send("{ERR}Intersects existing land ID# "+id);
            mess.send("{ERR}Selecting/showing land ID# "+id+" instead");
            iCoLand.tmpCuboidMap.put(((Player)sender).getName(), iCoLand.landMgr.getLandById(id).location );
            showExistingLandInfo(sender, landDB.getLandById(id));
	    } else {
            mess.send("{}"+Misc.headerify("{PRM}Unclaimed Land{}"));
            mess.send("Dimensoins: " + select.toDimString() );
            mess.send("Volume: " + select.volume() );
            mess.send("Price: " + iCoLand.df.format(getPrice(sender, select)));
	    }
	    
	}
	
	public void showSelectLandInfo(CommandSender sender, Integer id) {
	    showExistingLandInfo(sender, landDB.getLandById(id));
	}
	
	public void showExistingLandInfo(CommandSender sender, Land land) {
	    Messaging mess = new Messaging(sender);
	    mess.send("{}"+Misc.headerify("{} Land ID# {PRM}"+land.getID()+"{} --"+
	                                  (land.locationName.isEmpty()?"":" {PRM}"+land.locationName+" {}")
	                                 ));
	    mess.send("{CMD}C: {}"+land.location.toCenterCoords()+" {CMD}V: {}"+land.location.volume()+" {CMD}D: {}"+land.location.toDimString());
        mess.send("{CMD}Owner: {}"+land.owner);
        if ( !(sender instanceof Player) || land.owner.equals(((Player)sender).getName()) || iCoLand.hasPermission(sender,"bypass") ) {
            if ( !land.locationName.isEmpty() )
                mess.send("{CMD}Name: {}"+land.locationName);
            mess.send("{CMD}Created: {}"+land.dateCreated);
            mess.send("{CMD}Taxed: {}"+land.dateTaxed);
            mess.send("{CMD}Perms: {}"+Land.writePermTags(land.canBuildDestroy));            
            mess.send("{CMD}Addons: {}"+Land.writeAddonTags(land.addons));
            mess.send("{CMD}Addon Prices: {}"+Land.writeAddonPrices(sender, land));
        }
	}
	
	public double getPrice(CommandSender sender, Cuboid target) {
	    double sum = 0;
	    Integer sx = target.LocMax.getBlockX()-target.LocMin.getBlockX()+1;
	    Integer sy = target.LocMax.getBlockY()-target.LocMin.getBlockY()+1;
	    Integer sz = target.LocMax.getBlockZ()-target.LocMin.getBlockZ()+1;
	    for(int x=0;x<sx;x++) {
	        for(int y=0;y<sy;y++) {
	            for(int z=0;z<sz;z++) {
	                sum += getPriceOfBlock(sender, new Location(target.setLoc1.getWorld(), 
	                        target.LocMin.getBlockX()+x, target.LocMin.getBlockY()+y, target.LocMin.getBlockZ()+z));
	            }
	        }
	    }
	    return sum;
	}
	
	public double getPriceOfBlock(CommandSender sender, Location target) {
//	    if ( iCoLand.hasPermission(sender, "nocost") ) {
//	        return 0;
//	    } else {
	        return Config.pricePerBlockRaw;
//	    }
	}
	
	public boolean canClaimMoreLands(String playerName) {
        ArrayList<Land> lands = getLandsOwnedBy(playerName);
        Integer numClaimed = lands.size();
        return  ( numClaimed < Config.maxLandsClaimable );
	}
	
	public boolean canClaimMoreVolume(String playerName, Integer claimSize) {
        ArrayList<Land> lands = getLandsOwnedBy(playerName);
        Integer totalBlocks = claimSize;
        for(Land land : lands) {
            totalBlocks += land.location.volume();
        }
        return ( totalBlocks <= Config.maxBlocksClaimable );
	}
	
	
}

package me.slaps.iCoLand;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;

public class LandManager {
	
    Random rn = new Random();

    private LandDB landDB;
	
	public LandManager(LandDB db) {
	    landDB = db;
	    
	    if ( !iConomy.getBank().hasAccount(Config.bankName) ) {
	        iCoLand.info("Creating iConomy Tax/Bank account: "+Config.bankName);
	        iConomy.getBank().addAccount(Config.bankName);
	    } else {
	        iCoLand.info("Found iConomy Tax/Bank account: "+Config.bankName);
	    }
	}
	
	public void close() {
	    landDB.close();
	}
	
	public boolean addLand(Cuboid sl, String owner, String perms, String addons) {
	    if ( !sl.isValid() ) return false;
		if ( intersectsExistingLand(sl) > 0 ) return false;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		return landDB.createNewLand(new Land(0, sl, owner, "", Land.parsePermTags(perms), 
		        Land.parseAddonTags(addons), now, now, true));
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
	
	public ArrayList<Land> listLandPastTaxTime(Timestamp time) {
	    return landDB.listLandPastTaxTime(time);
	}
	
	public Location getCenterOfLand(Integer id) {
	    Land land = landDB.getLandById(id);
	    return land.getCenter();
	}
		
	public boolean isOwner(String playerName, Integer id) {
	    return getOwner(id).equals(playerName);
	}
	
	public boolean canBuildDestroy(Player player, Location loc) {

	    String playerName = player.getName();
	    Integer id = landDB.getLandId(loc);
	    if ( id > 0 ) {
	        return landDB.hasPermission(id, playerName);
	    } else {
	        if ( !Config.unclaimedLandCanBuild ) {
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
	
	public int countLandsOwnedBy(String playerName) {
	    return landDB.countLandOwnedBy(playerName);
	}
	
	public ArrayList<Land> getLandsOwnedBy(String playerName, int limit, int offset) {
	    return landDB.listLandOwnedBy(playerName, limit, offset);
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
            mess.send("Price: " + iCoLand.df.format(getPrice(select)));
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
	        return Config.pricePerBlock.get("raw");
	}
	
	public boolean canClaimMoreLands(String playerName) {
        return  ( landDB.countLandOwnedBy(playerName) < Config.maxLandsClaimable );
	}
	
	public boolean canClaimMoreVolume(String playerName, Integer claimSize) {
        ArrayList<Land> lands = getLandsOwnedBy(playerName, 0, 0);
        Integer totalBlocks = claimSize;
        for(Land land : lands) {
            totalBlocks += land.location.volume();
        }
        return ( totalBlocks <= Config.maxBlocksClaimable );
	}
	
    public void importDB(File importFile) {
        landDB.importDB(importFile);
    }

    public void exportDB(File exportFile) {
        landDB.exportDB(exportFile);
    }
    
    public boolean updateName(int id, String name) {
        return landDB.updateLandName(id, name);
    }
    
    public boolean updateOwner(int id, String playerName) {
        return landDB.updateLandOwner(id, playerName);
    }
    
    public boolean updateAddons(int id, String addonString) {
        return landDB.updateLandAddons(id, addonString);
    }
    
    public boolean updateTaxTime(int id, Timestamp time) {
        return landDB.updateTaxTime(id, time);
    }
    
    public boolean updateActive(int id, Boolean active) {
        return landDB.updateActive(id, active);
    }
    
    public boolean toggleAddons(int id, String tags) {
        Set<String> in = Land.parseAddonTags(tags).keySet();
        HashMap<String, Boolean> addons = Land.parseAddonTags(iCoLand.landMgr.getAddons(id));
        for(String addon : in ) {
            if ( addons.containsKey(addon) )
                addons.remove(addon);
            else
                addons.put(addon, true);
        }
        return updateAddons(id, Land.writeAddonTags(addons));
    }
    
    public boolean removeAddon(int id, String addon) {
        Boolean ret = false;
        HashMap<String, Boolean> addons = Land.parseAddonTags(iCoLand.landMgr.getAddons(id));
        if ( addons.containsKey(addon) ) {
            addons.remove(addon);
            ret = true;
        } else {
            ret = false;
        }
        updateAddons(id, Land.writeAddonTags(addons));
        return ret;
    }
    
    public boolean addAddon(int id, String addon) {
        Boolean ret = false;
        HashMap<String, Boolean> addons = Land.parseAddonTags(iCoLand.landMgr.getAddons(id));
        if ( addons.containsKey(addon) ) {
            ret = false;
        } else {
            addons.put(addon, true);
            ret = true;
        }
        updateAddons(id, Land.writeAddonTags(addons));
        return ret;
    }

    public boolean updatePerms(int id, String permString) {
        return landDB.updateLandPerms(id, permString);        
    }
    
    public boolean modifyPermTags(int id, String args) {
        HashMap<String, Boolean> perms = Land.parsePermTags(iCoLand.landMgr.getPerms(id));
        
        if ( args.isEmpty() ) return false;
        String[] split = args.split(" ");
        for(String tag : split ) {
            String[] keys = tag.split(":");
            if ( keys.length == 2 ) {
                if ( keys[1].equals("-") ) {
                    perms.remove(keys[0]);
                } else if ( keys[1].startsWith("f") ) {
                    perms.put(keys[0], false);
                } else if ( keys[1].startsWith("t") ) {
                    perms.put(keys[0], true);
                } else { 
                    iCoLand.warning("Error parsing tag: "+tag);
                }
            } else {
                iCoLand.warning("Error parsing tag: "+tag);
            }
        }
        return updatePerms(id, Land.writePermTags(perms));
    }
    
    public void modifyBuildDestroyWithTags(String tagString) {

    }
    
    public String getAddons(int id) {
        return landDB.getLandAddons(id);
    }
    
    public String getPerms(int id) {
        return landDB.getLandPerms(id);
    }
    
    public String getOwner(int id) {
        return landDB.getLandOwner(id);
    }
    
	public void test() {
        long start = System.currentTimeMillis();
        int numLands = 10000;
        for(int i=0;i<numLands;i++) {
            Location loc1 = new Location(iCoLand.server.getWorlds().get(0), rand(-10000,10000), 8, rand(-10000,10000));
            Location loc2 = new Location(iCoLand.server.getWorlds().get(0), loc1.getBlockX()+rand(0,100), loc1.getBlockY()+rand(0,120), loc1.getBlockZ()+rand(0,100));
            iCoLand.landMgr.addLand(new Cuboid(loc1,loc2), "kigam", "", "");
            if ( i % 100 == 0 ) {
                if ( Config.debugMode ) iCoLand.info(landDB.countLandOwnedBy(null)+" lands in the database");
            }
        }
        if ( Config.debugMode ) iCoLand.info("Inserting "+numLands+" random lands took: "+(System.currentTimeMillis()-start)+" ms");
        if ( Config.debugMode ) iCoLand.info(landDB.countLandOwnedBy(null)+" lands in the database");
	}
	
    public int rand(int lo, int hi) {
        int n = hi - lo + 1;
        int i = rn.nextInt() % n;
        if ( i < 0 ) 
            i = -i;
        return lo+i;
    }
	
}

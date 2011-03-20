package me.slaps.iConomyLand;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public class LandManager {
	
    iConomyLand parent;
	
	File shopFile;
	
	Boolean shopLocationsEnabled = false;
	
	private ArrayList<Land> lands;
	private Configuration ShopConfig;
	
	public LandManager(iConomyLand plug) {
		parent = plug;
		lands = new ArrayList<Land>();

		shopFile = new File(parent.getDataFolder() + File.separator + "lands.yml");
		
		ShopConfig = new Configuration(shopFile);
		
		if ( !shopFile.exists() ) saveConfigFile();
		
		loadConfigFile();
	}
	

	
	public void loadConfigFile() {
		ShopConfig.load();
		
		//shopLocationsEnabled = ShopConfig.getBoolean("enabled", false);
		List<String> oList = ShopConfig.getStringList("lands", null);
		
		iConomyLand.warning("Found " + oList.size() + " lands to protect ( loaded from file )");
		
		Iterator<String> itr = oList.iterator();
		while(itr.hasNext()) {
			String o = itr.next();
			
			LinkedHashMap<String,String> shopkeys = new LinkedHashMap<String,String>();
            
            String[] split = o.replaceFirst("\\{(.*)\\}","$1").split(",");
            for( String line : split ) {
                String[] ls = line.trim().split("=");
                shopkeys.put(ls[0].trim(), (ls.length>1)?ls[1].trim():"");
            }
			
			

            int id = Integer.parseInt(shopkeys.get("id"));
            
            Location loc1 = new Location(parent.getServer().getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner1x")), 
                                         Double.parseDouble(shopkeys.get("corner1y")), 
                                         Double.parseDouble(shopkeys.get("corner1z")) );
            Location loc2 = new Location(parent.getServer().getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner2x")), 
                                         Double.parseDouble(shopkeys.get("corner2y")), 
                                         Double.parseDouble(shopkeys.get("corner2z")) );
            Cuboid loc = new Cuboid(loc1, loc2);
            String owner = shopkeys.get("owner");
            String perms = shopkeys.get("perms");
            String dateCreated = shopkeys.get("dateCreated");
            String dateTaxed = shopkeys.get("dateTaxed");
            
            if ( !lands.add(new Land(id, loc, owner, perms, dateCreated, dateTaxed)) ) {
                iConomyLand.warning("Error reading land.yml! Corrupted data?");
            }

		}
		
		saveConfigFile();
		
	}
	
	public void saveConfigFile() {		
		ShopConfig = new Configuration(shopFile);
		
		ArrayList<LinkedHashMap<String,Object>> tmpshops = new ArrayList<LinkedHashMap<String,Object>>();
		
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
			Land shop = itr.next();
			LinkedHashMap<String,Object> tmpmap = new LinkedHashMap<String,Object>();
			
			tmpmap.put("id", shop.getID());
			tmpmap.put("owner", shop.owner);
			tmpmap.put("perms", shop.perms);
            tmpmap.put("dateCreated", shop.dateCreated.toString() );
            tmpmap.put("dateTaxed", shop.dateTaxed.toString() );
			tmpmap.put("world", shop.location.setLoc1.getWorld().getName());
			tmpmap.put("corner1x",shop.location.setLoc1.getBlockX());
			tmpmap.put("corner1y",shop.location.setLoc1.getBlockY());
			tmpmap.put("corner1z",shop.location.setLoc1.getBlockZ());
			tmpmap.put("corner2x",shop.location.setLoc2.getBlockX());
			tmpmap.put("corner2y",shop.location.setLoc2.getBlockY());
			tmpmap.put("corner2z",shop.location.setLoc2.getBlockZ());

			tmpshops.add(tmpmap);			
		}
		ShopConfig.setProperty("lands", tmpshops);
		
		ShopConfig.save();
	}
	
	public void enableShopLocations() {
		shopLocationsEnabled = true;
		saveConfigFile();
	}

	public void disableShopLocations() {
		shopLocationsEnabled = false;
		saveConfigFile();
	}	
	
	public boolean add(Cuboid sl, String owner, String perms) {
	    if ( !sl.isValid() ) return false;
		if ( intersectsExistingLand(sl) ) return false;
		Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());
		lands.add( new Land(getNextID(), sl, owner, perms, now.toString(), now.toString()) );
		saveConfigFile();
		return true;
	}
	
	public boolean removeLandByID(Integer id) {
		int i = 0;
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.getID() == id ) {
			    lands.remove(i);
				saveConfigFile();
				return true;
			}
			i++;
		}
		return false;

	}
	
	public String listLand() {
		String out = "";
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			out += tmp.getID();
			out += itr.hasNext() ? ", " : "";
		}
		return out;
	}
	
	// just gets corner 1 for now
	public Location getCenterOfLand(Integer id) {
		Iterator<Land> itr = lands.iterator();
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
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.getID() > i ) i = tmp.getID();
		}
		return i+1;
	}
	
	public boolean inLand(Location loc) {
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.contains(loc) ) return true;
		}
		return false;
	}
	
	public Integer getLandID(Location loc) {
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.contains(loc) ) return tmp.getID();
		}
		return -1;
	}
	
	public boolean intersectsExistingLand(Cuboid loc) {
		Iterator<Land> itr = lands.iterator();
		while(itr.hasNext()) {
		    Land tmp = itr.next();
			if ( tmp.intersects(loc) ) return true;
		}
		return false;
	}
	
	
}

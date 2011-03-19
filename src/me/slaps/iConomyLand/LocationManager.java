package me.slaps.DMWrapper;

import java.io.File;
import java.util.List;
//import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
//import java.util.logging.Logger;

//import org.bukkit.Server;
//import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public class LocationManager {
	
	DMWrapper parent;
	
	File shopFile;
	
	Boolean shopLocationsEnabled = false;
	
	private ArrayList<ShopLocation> shops;
	private Configuration ShopConfig;
	
	public LocationManager(DMWrapper plug) {
		parent = plug;
		shops = new ArrayList<ShopLocation>();

		shopFile = new File(parent.getDataFolder() + File.separator + "shops.yml");
		
		ShopConfig = new Configuration(shopFile);
		
		if ( !shopFile.exists() ) {
//			try {
//				shopFile.createNewFile();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			saveConfigFile();
		} 
		
		loadConfigFile();
	}
	

	
	public void loadConfigFile() {
		ShopConfig.load();
		
		shopLocationsEnabled = ShopConfig.getBoolean("enabled", false);

		List<String> oList = ShopConfig.getStringList("shops", null);
		
		Iterator<String> itr = oList.iterator();
		while(itr.hasNext()) {
			String o = itr.next();
			
			LinkedHashMap<String,String> shopkeys = new LinkedHashMap<String,String>();
			StringTokenizer st = new StringTokenizer(o, "{}=, ");
			while (st.hasMoreTokens()) shopkeys.put(st.nextToken(),st.nextToken());
			
			Location loc1 = new Location(parent.getServer().getWorld(shopkeys.get("world")), 
					                     Double.parseDouble(shopkeys.get("corner1x")), 
					                     Double.parseDouble(shopkeys.get("corner1y")), 
					                     Double.parseDouble(shopkeys.get("corner1z")) );
			Location loc2 = new Location(parent.getServer().getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner2x")), 
                                         Double.parseDouble(shopkeys.get("corner2y")), 
                                         Double.parseDouble(shopkeys.get("corner2z")) );
			add(new ShopLocation(Integer.parseInt(shopkeys.get("id")), loc1, loc2));

		}
		
		saveConfigFile();
		
	}
	
	public void saveConfigFile() {		
		ShopConfig = new Configuration(shopFile);
		
		ArrayList<LinkedHashMap<String,Object>> tmpshops = new ArrayList<LinkedHashMap<String,Object>>();
		
		ShopConfig.setProperty("enabled", shopLocationsEnabled);
		
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation shop = itr.next();
			LinkedHashMap<String,Object> tmpmap = new LinkedHashMap<String,Object>();
			
			tmpmap.put("id", shop.id);
			tmpmap.put("world", shop.setLoc1.getWorld().getName());
			tmpmap.put("corner1x",shop.setLoc1.getBlockX());
			tmpmap.put("corner1y",shop.setLoc1.getBlockY());
			tmpmap.put("corner1z",shop.setLoc1.getBlockZ());
			tmpmap.put("corner2x",shop.setLoc2.getBlockX());
			tmpmap.put("corner2y",shop.setLoc2.getBlockY());
			tmpmap.put("corner2z",shop.setLoc2.getBlockZ());

			tmpshops.add(tmpmap);			
		}
		ShopConfig.setProperty("shops", tmpshops);
		
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
	
	public boolean add(ShopLocation sl) {
		if ( intersectsExistingShop(sl) ) return false;
		if ( sl.id == null ) sl.id = getNextID();
		shops.add(sl);
		saveConfigFile();
		return true;
	}
	
	public boolean removeShopByID(Integer id) {
		int i = 0;
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			if ( tmp.id == id ) {
				shops.remove(i);
				saveConfigFile();
				return true;
			}
			i++;
		}
		return false;

	}
	
	public String listShops() {
		String out = "";
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			out += tmp.id;
			out += itr.hasNext() ? ", " : "";
		}
		return out;
	}
	
	public Location getCenterOfShop(Integer id) {
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			if ( id.equals(tmp.id) ) {
				Location dest = new Location(tmp.setLoc1.getWorld(), tmp.setLoc1.getBlockX(), tmp.setLoc1.getBlockY(), tmp.setLoc1.getBlockZ() );
				dest.setY(dest.getY()+1);
				return dest;
			}
		}
		return null;
	}
	
	public Integer getNextID() {
		Integer i = 0;
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			if ( tmp.id > i ) i = tmp.id;
		}
		return i+1;
	}
	
	public boolean inShopLoc(Location loc) {
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			if ( tmp.isInShop(loc) ) return true;
		}
		return false;
	}
	
	public Integer getShopID(Location loc) {
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			if ( tmp.isInShop(loc) ) return tmp.id;
		}
		return -1;
	}
	
	public boolean intersectsExistingShop(ShopLocation loc) {
		Iterator<ShopLocation> itr = shops.iterator();
		while(itr.hasNext()) {
			ShopLocation tmp = itr.next();
			if ( tmp.intersectsShop(loc) ) return true;
		}
		return false;
	}
	
}

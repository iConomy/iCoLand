package me.slaps.iConomyLand;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public class LandDBFlatFile extends LandDB {
    
    private File landConfigFile;
    private Configuration LandConfig;

    
    public LandDBFlatFile(File configFile) {
        super();
        landConfigFile = configFile; //new File(parent.getDataFolder() + File.separator + "lands.yml");
        LandConfig = new Configuration(landConfigFile);
        load();
    }

    public void load() {
        LandConfig.load();
        
        List<String> oList = LandConfig.getStringList("lands", null);
        
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
            
            Location loc1 = new Location(iConomyLand.server.getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner1x")), 
                                         Double.parseDouble(shopkeys.get("corner1y")), 
                                         Double.parseDouble(shopkeys.get("corner1z")) );
            Location loc2 = new Location(iConomyLand.server.getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner2x")), 
                                         Double.parseDouble(shopkeys.get("corner2y")), 
                                         Double.parseDouble(shopkeys.get("corner2z")) );
            Cuboid loc = new Cuboid(loc1, loc2);
            String owner = shopkeys.get("owner");
            HashMap<String, Boolean> perms = Land.parsePermTags(shopkeys.get("perms"));
            HashMap<String, Boolean> addons = Land.parseAddonTags(shopkeys.get("addons"));
            String dateCreated = shopkeys.get("dateCreated");
            String dateTaxed = shopkeys.get("dateTaxed");

            lands.put(id, new Land(id, loc, owner, perms, addons, dateCreated, dateTaxed));

        }
        
        save();
        
    }
    
    public void save() {
        LandConfig = new Configuration(landConfigFile);
        
        ArrayList<LinkedHashMap<String,Object>> tmpshops = new ArrayList<LinkedHashMap<String,Object>>();
        Iterator<Land> itr = lands.values().iterator();
        while(itr.hasNext()) {
            Land shop = itr.next();
            LinkedHashMap<String,Object> tmpmap = new LinkedHashMap<String,Object>();
            
            tmpmap.put("id", shop.getID());
            tmpmap.put("owner", shop.owner);
            tmpmap.put("perms", Land.writePermTags(shop.canBuildDestroy));
            tmpmap.put("addons", Land.writeAddonTags(shop.addons));
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
        LandConfig.setProperty("lands", tmpshops);
        
        LandConfig.save();
    }
    

    
    
    
    
    
}

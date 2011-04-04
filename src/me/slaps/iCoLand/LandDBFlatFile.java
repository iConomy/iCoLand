package me.slaps.iCoLand;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public class LandDBFlatFile implements LandDB {
    
    private File landConfigFile;
    private Configuration LandConfig;

    public HashMap<Integer,Land> lands;
    
    public LandDBFlatFile() {
        lands = new HashMap<Integer,Land>();
    }
    
    public LandDBFlatFile(File configFile) {
        super();
        landConfigFile = configFile; //new File(parent.getDataFolder() + File.separator + "lands.yml");
        LandConfig = new Configuration(landConfigFile);
        load();
    }

    public void load() {
        LandConfig.load();
        
        List<String> oList = LandConfig.getStringList("lands", null);
        
        iCoLand.warning("Found " + oList.size() + " lands to protect ( loaded from file )");
        
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
            
            Location loc1 = new Location(iCoLand.server.getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner1x")), 
                                         Double.parseDouble(shopkeys.get("corner1y")), 
                                         Double.parseDouble(shopkeys.get("corner1z")) );
            Location loc2 = new Location(iCoLand.server.getWorld(shopkeys.get("world")), 
                                         Double.parseDouble(shopkeys.get("corner2x")), 
                                         Double.parseDouble(shopkeys.get("corner2y")), 
                                         Double.parseDouble(shopkeys.get("corner2z")) );
            Cuboid loc = new Cuboid(loc1, loc2);
            String owner = shopkeys.get("owner");
            HashMap<String, Boolean> perms = Land.parsePermTags(shopkeys.get("perms"));
            HashMap<String, Boolean> addons = Land.parseAddonTags(shopkeys.get("addons"));
            String dateCreated = shopkeys.get("dateCreated");
            String dateTaxed = shopkeys.get("dateTaxed");
            String locationName = shopkeys.get("name");

            lands.put(id, new Land(id, loc, owner, locationName, perms, addons, Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateTaxed)));

        }
        
        save();
        
    }
    
    public void save() {
        LandConfig = new Configuration(landConfigFile);
        
        ArrayList<LinkedHashMap<String,Object>> tmpshops = new ArrayList<LinkedHashMap<String,Object>>();
        Iterator<Land> itr = lands.values().iterator();
        while(itr.hasNext()) {
            Land land = itr.next();
            LinkedHashMap<String,Object> tmpmap = new LinkedHashMap<String,Object>();
            
            tmpmap.put("id", land.getID());
            tmpmap.put("owner", land.owner);
            tmpmap.put("perms", Land.writePermTags(land.canBuildDestroy));
            tmpmap.put("addons", Land.writeAddonTags(land.addons));
            tmpmap.put("dateCreated", land.dateCreated.toString() );
            tmpmap.put("dateTaxed", land.dateTaxed.toString() );
            tmpmap.put("name", land.locationName);
            tmpmap.put("world", land.location.setLoc1.getWorld().getName());
            tmpmap.put("corner1x",land.location.setLoc1.getBlockX());
            tmpmap.put("corner1y",land.location.setLoc1.getBlockY());
            tmpmap.put("corner1z",land.location.setLoc1.getBlockZ());
            tmpmap.put("corner2x",land.location.setLoc2.getBlockX());
            tmpmap.put("corner2y",land.location.setLoc2.getBlockY());
            tmpmap.put("corner2z",land.location.setLoc2.getBlockZ());

            tmpshops.add(tmpmap);           
        }
        LandConfig.setProperty("lands", tmpshops);
        
        LandConfig.save();
    }


    public boolean createNewLand(Land newLand) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean removeLandById(int id) {
        // TODO Auto-generated method stub
        return false;
    }


    public ArrayList<Land> listAllLand() {
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<Land> listLandOwnedBy(String playerName) {
        // TODO Auto-generated method stub
        return null;
    }


    public int getLandId(Location loc) {
        // TODO Auto-generated method stub
        return 0;
    }


    public Land getLandById(int id) {
        // TODO Auto-generated method stub
        return null;
    }


    public String getLandPerms(int id) {
        // TODO Auto-generated method stub
        return null;
    }


    public String getLandAddons(int id) {
        // TODO Auto-generated method stub
        return null;
    }


    public String getLandOwner(int id) {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean updateLandOwner(int id, String newOwner) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean updateLandName(int id, String newName) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean updateLandPerms(int id, HashMap<String, Boolean> newPerms) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean updateLandAddons(int id, HashMap<String, Boolean> newAddons) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean hasPermission(int id, String playerName) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean landIdExists(int id) {
        // TODO Auto-generated method stub
        return false;
    }


    public int intersectsExistingLand(Cuboid loc) {
        // TODO Auto-generated method stub
        return 0;
    }



    public void importDB(File landYMLFile) {
        // TODO Auto-generated method stub
        
    }

    public void exportDB(File landYMLFile) {
        // TODO Auto-generated method stub
        
    }
    

    
    
    
    
    
}

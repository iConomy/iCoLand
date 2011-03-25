package me.slaps.iConomyLand;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;

public class Land {
    private int id;                                     // unique # ( sql primary key )
    public Cuboid location;                             // cuboid describing location
    public String owner;                                // owner name
    public String locationName;                         // location name ( for announce )
    public HashMap<String, Boolean> canBuildDestroy;    // hash map lookup for perms
    public HashMap<String, Boolean> addons;             // hash map lookup for addons
    public Timestamp dateCreated;                       // date created
    public Timestamp dateTaxed;                         // date taxed
    private boolean valid = false;                      // valid
    
    public Land(int id, Cuboid loc, String owner, HashMap<String, Boolean> perms, 
            HashMap<String, Boolean> addons, String dateCreated, String dateTaxed) {
        
        this.id = id;
        this.location = loc;
        this.owner = owner;
        this.canBuildDestroy = perms;
        this.addons = addons;
        
        this.dateCreated = Timestamp.valueOf(dateCreated);
        this.dateTaxed = Timestamp.valueOf(dateTaxed);
        
        validate();
    }
    
    public int getID() {
        return id;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean validate() {
        boolean ret = true;
        if ( id < 0 ) ret = false;
        if ( !location.isValid() ) ret = false;
        if ( owner.isEmpty() ) ret = false;
//        if ( !validatePermString(perms) ) ret = false;
        if ( dateCreated == null ) ret = false;
        if ( dateTaxed == null ) ret = false;
        
        valid = ret;
        return ret;
    }
    
//    public static boolean validatePermString(String perms) {
//        return true;
//    }
    
    public boolean contains(Location loc) {
        return location.isIn(loc);
    }
    
    public boolean intersects(Cuboid other) {
        return location.intersects(other);
    }
    
    public boolean hasPermission(String playerName) {
        Boolean ret = false;
        if (playerName.equals(owner)) ret = true;
        else { 
            if ( canBuildDestroy.containsKey("all") ) {
                ret = canBuildDestroy.get("all");
            }
            if ( canBuildDestroy.containsKey(playerName) ) {
                ret = canBuildDestroy.get(playerName);
            }
            
        }
        if (iConomyLand.debugMode) iConomyLand.info("Player "+playerName+(ret?" has perms ":" doesn't have perms ")+"in land ID# "+id);
        return ret;
    }

    public boolean hasAddon(String addon) {
        if ( addons.containsKey(addon) ) {
            return addons.get(addon);
        } else {
            return false;
        }
    }
    
    public void addPermission(String playerName, Boolean perm) {
        canBuildDestroy.put(playerName, perm);
    }
    
    public void delPermission(String playerName) {
        if ( canBuildDestroy.containsKey(playerName) )
            canBuildDestroy.remove(playerName);
    }
    
    public void giveAddon(String addon) {
        addons.put(addon, new Boolean(true));
    }
    
    public void removeAddon(String addon) {
        addons.remove(addon);
    }
    
    public static String writeAddonTags(HashMap<String, Boolean> addonTags) {
        String ret = "";
        Set<String> tags = addonTags.keySet();
        for(String tag : tags) {
            ret += tag + " ";
        }
        return ret;
    }
    
    
    public static HashMap<String, Boolean> parseAddonTags(String tagString) {
        HashMap<String, Boolean> ret = new HashMap<String, Boolean>();
        if ( tagString.isEmpty() ) return ret;
        String[] split = tagString.split(" ");
        for(String tag : split) {
            ret.put(tag, new Boolean(true));
        }
        return ret;        
    }
    
    public static String writePermTags(HashMap<String, Boolean> permTags) {
        String ret = "";
        Set<String> tags = permTags.keySet();
        for(String tag : tags) {
            String perm = (permTags.get(tag))?"t":"f";
            ret += tag + ":" + perm + " ";
        }
        return ret;
    }
    

    public static HashMap<String, Boolean> parsePermTags(String tagString) {
        HashMap<String, Boolean> ret = new HashMap<String, Boolean>();
        if ( tagString.isEmpty() ) return ret;
        String[] split = tagString.split(" ");
        for(String tag : split) {
            String[] keys = tag.split(":");
            if ( keys.length == 2 ) {
                Boolean perm = keys[1].toLowerCase().startsWith("t");
                ret.put(keys[0], perm);
            } else {
                iConomyLand.warning("Error parsing tag from flat file: "+tag);
            }
        }
        return ret;
    }
    
    public double getAddonPrice(String addon) {
        if ( addon.equalsIgnoreCase("announce") )
            return iConomyLand.pricePerBlockAddonAnnounce*location.volume();
        else if ( addon.equalsIgnoreCase("heal") )
            return iConomyLand.pricePerBlockAddonHealing*location.volume();
        else if ( addon.equalsIgnoreCase("noenter") )
            return iConomyLand.pricePerBlockAddonNoEnter*location.volume();
        else
            return 0;
    }
    
    public static String writeAddonPrices(Land land) {
        DecimalFormat df = new DecimalFormat("0.##");
        String ret = "";
        if ( !land.addons.containsKey("announce") )
            ret += "Announce: "+df.format(land.getAddonPrice("announce"))+" ";
        if ( !land.addons.containsKey("heal") )
            ret += "Heal: "+df.format(land.getAddonPrice("heal"))+" ";
        if ( !land.addons.containsKey("noenter") )
            ret += "NoEnter: "+df.format(land.getAddonPrice("noenter"))+" ";
        
        return ret;
    }
    
    public Location getCenter() {
        return location.getCenter();
    }
    
    public void addAddon(String addon) {
        addons.put(addon, new Boolean(true));
    }
    
    
}

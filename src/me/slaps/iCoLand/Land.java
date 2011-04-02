package me.slaps.iCoLand;

import java.sql.Timestamp;
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
    
    public Land(int id, Cuboid loc, String owner, String locName, HashMap<String, Boolean> perms, 
            HashMap<String, Boolean> addons, String dateCreated, String dateTaxed) {
        
        this.id = id;
        this.location = loc;
        this.owner = owner;
        this.canBuildDestroy = perms;
        this.addons = addons;
        
        this.dateCreated = Timestamp.valueOf(dateCreated);
        this.dateTaxed = Timestamp.valueOf(dateTaxed);
        
        this.locationName = locName;
        
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
        if ( dateCreated == null ) ret = false;
        if ( dateTaxed == null ) ret = false;
        
        valid = ret;
        return ret;
    }
    
    public void changeOwner(String newOwner) {
        owner = newOwner;
    }
    

    public void addPermCanBuild(String playerName, Boolean perm) {
        canBuildDestroy.put(playerName, perm);
    }
    
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
            if ( canBuildDestroy.containsKey(playerName) ) {
                ret = canBuildDestroy.get(playerName);
            }
            if ( canBuildDestroy.containsKey("default") ) {
                ret = canBuildDestroy.get("default");
            }
        }
        if (Config.debugMode) iCoLand.info("Player "+playerName+(ret?" has perms ":" doesn't have perms ")+"in land ID# "+id);
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
        addons.put(addon, true);
    }
    
    public void removeAddon(String addon) {
        addons.remove(addon);
    }
    

    
    public Location getCenter() {
        return location.getCenter();
    }
    
    public void addAddon(String addon) {
        addons.put(addon, true);
    }

    public double getAddonPrice(String addon) {
        if ( Config.isAddon(addon) ) {
            return Config.addonsPricePerBlock.get(addon)*location.volume();
        } else {
            return 0;
        }
    }
    
    public double getSalePrice() {
        double price = 0;
        Set<String> addonsBought = addons.keySet();
        
        // add up prices for addons
        for( String add : addonsBought ) {
            price += getAddonPrice(add);
        }
        
        // add price of land
        price += iCoLand.landMgr.getPrice(location);
        
        // take out sales tax
        price *= Config.sellTax;
        
        return price;
    }
    
    
    public void modifyBuildDestroyWithTags(String tagString) {
        if ( tagString.isEmpty() ) return;
        String[] split = tagString.split(" ");
        for(String tag : split ) {
            String[] keys = tag.split(":");
            if ( keys.length == 2 ) {
                if ( keys[1].equals("-") ) {
                    canBuildDestroy.remove(keys[0]);
                } else if ( keys[1].startsWith("f") ) {
                    canBuildDestroy.put(keys[0], false);
                } else if ( keys[1].startsWith("t") ) {
                    canBuildDestroy.put(keys[0], true);
                } else { 
                    iCoLand.warning("Error parsing tag: "+tag);
                }
            } else {
                iCoLand.warning("Error parsing tag: "+tag);
            }
        }
    }
    
    
    
    
    //  STATICS =============================================================
    
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
            ret.put(tag, true);
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
                iCoLand.warning("Error parsing tag: "+tag);
            }
        }
        return ret;
    }
    
    public static String writeAddonPrices(Land land) {
        String ret = "";
        if ( !land.addons.containsKey("announce") )
            ret += "Announce: "+iCoLand.df.format(land.getAddonPrice("announce"))+" ";
        if ( !land.addons.containsKey("heal") )
            ret += "Heal: "+iCoLand.df.format(land.getAddonPrice("heal"))+" ";
        if ( !land.addons.containsKey("noenter") )
            ret += "NoEnter: "+iCoLand.df.format(land.getAddonPrice("noenter"))+" ";
        if ( !land.addons.containsKey("nospawn") )
            ret += "NoSpawn: "+iCoLand.df.format(land.getAddonPrice("nospawn"))+" ";
        
        return ret;
    }
    
    
    
}

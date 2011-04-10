package me.slaps.iCoLand;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class Land {
    private int id;                                     // unique # ( sql primary key )
    public Cuboid location;                             // cuboid describing location
    public String owner;                                // owner name
    public String locationName;                         // location name ( for announce )
    public HashMap<String, Boolean> canBuildDestroy;    // hash map lookup for perms
    public HashMap<String, Boolean> addons;             // hash map lookup for addons
    public Timestamp dateCreated;                       // date created
    public Timestamp dateTaxed;                         // date taxed
    public boolean active;                              // land active? ( inactive = didn't pay taxes )
    
    private boolean valid = false;                      // valid
    
    public Land(int id, Cuboid loc, String owner, String locName, HashMap<String, Boolean> perms, 
            HashMap<String, Boolean> addons, Timestamp dateCreated, Timestamp dateTaxed, Boolean active) {
        
        this.id = id;
        this.location = loc;
        this.owner = owner;
        this.canBuildDestroy = perms;
        this.addons = addons;
        
        this.dateCreated = dateCreated;
        this.dateTaxed = dateTaxed;
        
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

    public boolean contains(Location loc) {
        return location.isIn(loc);
    }
    
    public boolean intersects(Cuboid other) {
        return location.intersects(other);
    }
    
    public boolean hasPermission(String playerName) {
        Boolean ret = false;
        if ( !active ) ret = true;
        else if (playerName.equals(owner)) ret = true;
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
    
    public Location getCenter() {
        return location.getCenter();
    }

    public double getAddonPrice(String addon) {
        if ( Config.isAddon(addon) ) {
            return Config.pricePerBlock.get(addon)*location.volume();
        } else {
            return 0;
        }
    }
    
    public double getTotalPrice() {
        double price = 0;
        Set<String> addonsBought = addons.keySet();
        
        // add up prices for addons
        for( String add : addonsBought ) {
            price += getAddonPrice(add);
        }
        
        // add price of land
        price += iCoLand.landMgr.getPrice(location);
        
        // take out sales tax
        //price *= Config.sellTax;
        
        return price;
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
    
    public static String writeAddonPrices(CommandSender sender, Land land) {
        String ret = "";
        if ( !land.addons.containsKey("announce") )
            ret += "Announce: "+iCoLand.df.format(land.getAddonPrice("announce"))+" ";
        if ( !land.addons.containsKey("heal") )
            ret += "Heal: "+iCoLand.df.format(land.getAddonPrice("heal"))+" ";
        if ( !land.addons.containsKey("noenter") )
            ret += "NoEnter: "+iCoLand.df.format(land.getAddonPrice("noenter"))+" ";
        if ( !land.addons.containsKey("nospawn") )
            ret += "NoSpawn: "+iCoLand.df.format(land.getAddonPrice("nospawn"))+" ";
        if ( !land.addons.containsKey("noboom") )
            ret += "NoBoom: "+iCoLand.df.format(land.getAddonPrice("noboom"))+" ";
        if ( !land.addons.containsKey("nofire") )
            ret += "NoFire: "+iCoLand.df.format(land.getAddonPrice("nofire"))+" ";
        if ( !land.addons.containsKey("noflow") )
            ret += "NoFlow: "+iCoLand.df.format(land.getAddonPrice("noflow"))+" ";
        
        return ret;
    }
    
    
    
}

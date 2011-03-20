package me.slaps.iConomyLand;

import java.sql.Timestamp;

import org.bukkit.Location;

public class Land {
    private int id;             // unique # ( sql primary key )
    public Cuboid location;    // cuboid describing location
    public String owner;       //
    public String perms;
    public String addons;
    public Timestamp dateCreated;
    public Timestamp dateTaxed;
    private boolean valid = false;
    
    public Land(int id, Cuboid loc, String owner, String perms, String dateCreated, String dateTaxed) {
        this.id = id;
        this.location = loc;
        this.owner = owner;
        this.perms = perms;
        
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
        if ( !validatePermString(perms) ) ret = false;
        if ( dateCreated == null ) ret = false;
        if ( dateTaxed == null ) ret = false;
        
        valid = ret;
        return ret;
    }
    
    public static boolean validatePermString(String perms) {
        return true;
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
            String[] split = perms.split("");
            for( String perm : split )
                if ( perm.equals(playerName) ) ret = true;
        }
        if (iConomyLand.debugMode) iConomyLand.info("Player "+playerName+(ret?" has perms ":" doesn't have perms ")+"in land ID# "+id);
        return ret;
    }
}

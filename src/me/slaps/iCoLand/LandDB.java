package me.slaps.iCoLand;

import java.util.HashMap;

import org.bukkit.Location;

public abstract class LandDB {

    public HashMap<Integer,Land> lands;
    
    public LandDB() {
        lands = new HashMap<Integer,Land>();
    }
    
    abstract public int createNewLand(Land newLand);
    abstract public boolean removeLandById(int id);
    
    abstract public Land[] listAllLand();
    abstract public Land[] listLandOwnerBy();
    
    abstract public int getLandId(Location loc);

    abstract public Land getLandById(int id);
    abstract public String getLandPerms(int id);
    abstract public String getLandAddons(int id);
    abstract public String getLandOwner(int id);
    
}

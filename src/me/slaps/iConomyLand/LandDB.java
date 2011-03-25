package me.slaps.iConomyLand;

import java.util.HashMap;

public abstract class LandDB {

    public HashMap<Integer,Land> lands;
    
    public LandDB() {
        lands = new HashMap<Integer,Land>();
    }
    
    abstract public void load();
    
    abstract public void save();
    
}

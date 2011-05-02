package me.slaps.iCoLand;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache<K,V> extends LinkedHashMap<K,V> {
    
    private static final long serialVersionUID = 1L;
    
    Integer targetSize;
    
    public Cache(Integer targetSize) {
        this.targetSize = targetSize;
    }
    
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return ( this.size() > targetSize );
    }
}

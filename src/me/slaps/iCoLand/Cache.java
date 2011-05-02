package me.slaps.iCoLand;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache<K,V> extends LinkedHashMap<K,V> {
    
    private static final long serialVersionUID = 1L;
    
    Integer targetSize;
    
    long hit;
    long miss;
    
    public Cache(Integer targetSize) {
        super();
        this.targetSize = targetSize;
        this.hit = 0;
        this.miss = 0;
    }
    
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        if ( this.size() > targetSize ) {
            //iCoLand.info("Cache hit max size! "+targetSize);
            return true;
        } else
            return false;
    }
    
    
    public V put( K key, V value ) {
        miss++;
        iCoLand.info("Cache miss - size: "+this.size()+" "+(hit/1.0/((hit+miss))));
        return super.put( key, value );
    }
    
    public V get( Object key ) {
        hit++;
        iCoLand.info("Cache hit - size: "+this.size()+" "+(hit/1.0/((hit+miss))));
        return super.get( key );
    }
    
    public boolean containsKey( Object key ) {
        //iCoLand.info("Cache contains - size: "+this.size());        
        return super.containsKey( key );
    }
    
    
}

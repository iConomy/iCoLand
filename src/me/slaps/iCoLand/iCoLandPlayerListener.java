package me.slaps.iCoLand;

import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class iCoLandPlayerListener extends PlayerListener {
	
    private static HashMap<String, Long> timeMap;
    private static HashMap<String, Integer> locMap;
    private static HashMap<String, Location> lastNonLandLoc;
    private static int checkDelay = 300; // milliseconds
    
    
	public iCoLandPlayerListener(iCoLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.Monitor, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Monitor, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Monitor, plug);

	    timeMap = new HashMap<String, Long>();
	    locMap = new HashMap<String, Integer>();
	    lastNonLandLoc = new HashMap<String, Location>();
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
	    Player player = event.getPlayer();
	    String playerName = player.getName();
	    
	    if ( event.getAction().equals(Action.LEFT_CLICK_BLOCK) ) {
    	    if ( iCoLand.cmdMap.containsKey(playerName) && iCoLand.cmdMap.get(playerName).equals("select") ) {
    	        Cuboid newCuboid;
    	        Location loc = event.getClickedBlock().getLocation();
          
    	        if ( iCoLand.tmpCuboidMap.containsKey(playerName) ) {
    	            newCuboid = iCoLand.tmpCuboidMap.get(playerName);
    	            newCuboid.setLocation(loc);
    	        } else {
    	            newCuboid = new Cuboid(loc); 
    	        }
          
    	        Messaging mess = new Messaging(player);
          
    	        if ( newCuboid.isValid() ) {
    	            mess.send("{}Land selected!");
    	            iCoLand.landMgr.showSelectLandInfo(player, newCuboid);
    	            iCoLand.cmdMap.remove(playerName);
    	        } else {
    	            iCoLand.tmpCuboidMap.put(playerName, newCuboid);
    	            mess.send("{}Left click the 2nd corner");
    	        }
    	    }
	    }
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
	    Player player = event.getPlayer();

	    if ( !checkNow(player) ) return;

        String playerName = player.getName();
	    
	    if (!locMap.containsKey(playerName)) locMap.put(playerName, iCoLand.landMgr.getLandId(player.getLocation()) );
	    
	    int locFrom = locMap.get(playerName);
		int locTo = iCoLand.landMgr.getLandId(player.getLocation());
		
		if ( locTo == 0 ) lastNonLandLoc.put(playerName, player.getLocation());
		
		locMap.put(playerName, locTo);

        Land landFrom = iCoLand.landMgr.getLandById(locFrom);
        Land landTo = iCoLand.landMgr.getLandById(locTo);
        
		if ( Config.addonsEnabled.get("noenter") && 
		     landTo != null && locTo != 0 && landTo.hasAddon("noenter") && !landTo.hasPermission(playerName) && 
		     !iCoLand.hasPermission(player, "bypass") ) {
            Location loc = lastNonLandLoc.get(playerName);
            if ( loc != null ) {
                locMap.put(playerName, 0);
                player.sendMessage("Can't enter this land");
                player.teleport(loc);
            }
		} else if ( Config.addonsEnabled.get("noenter") && locFrom != locTo ) {
		    if ( locFrom != 0 ) {
		            if ( landFrom.hasAddon("announce") ) {
		            player.sendMessage("Leaving "+(landFrom.locationName.isEmpty()?"unnamed land":landFrom.locationName));
		        }
		    }
		    if ( locTo != 0 ) {
                if ( landTo.hasAddon("announce") ) {
                    player.sendMessage("Entering "+(landTo.locationName.isEmpty()?"unnamed land":landTo.locationName));
                }
		    }
		}
		
	}
	
	public boolean checkNow(Player player) {
	    String playerName = player.getName();
	    Long now = Calendar.getInstance().getTimeInMillis();  
	    
        if ( (!timeMap.containsKey(playerName)) 
                || (now > timeMap.get(playerName)) ) {
            timeMap.put(playerName, Calendar.getInstance().getTimeInMillis() + checkDelay);
            return true;
        } else {
            return false;
        }
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
	    iCoLand.cmdMap.remove(event.getPlayer().getName());
	    iCoLand.tmpCuboidMap.remove(event.getPlayer().getName());
	}


    
    

}
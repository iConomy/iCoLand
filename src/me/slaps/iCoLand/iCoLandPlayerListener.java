package me.slaps.iCoLand;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class iCoLandPlayerListener extends PlayerListener {
	
    private static HashMap<String, Long> timeMap;
    private static HashMap<String, Integer> locMap;
    private static HashMap<String, Location> lastNonLandLoc;
    
    
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
                    if ( Config.allLandFullHeight ) newCuboid.setFullHeight();
    	            mess.send("{}Land selected!");
    	            iCoLand.commandListener.showSelectLandInfo(player, newCuboid);
    	            iCoLand.cmdMap.remove(playerName);
    	        } else {
    	            iCoLand.tmpCuboidMap.put(playerName, newCuboid);
    	            mess.send("{}Left click the 2nd corner");
    	        }
    	    }
	    } else if ( event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
	        Material mat = event.getMaterial();
	        if ( mat.equals(Material.WATER_BUCKET) || mat.equals(Material.WATER) || 
	             mat.equals(Material.LAVA_BUCKET) || mat.equals(Material.LAVA) ) {
	            Location loc = event.getClickedBlock().getLocation();
                BlockFace mod = event.getBlockFace();
                loc.setX(loc.getX()+mod.getModX());
                loc.setY(loc.getY()+mod.getModY());
                loc.setZ(loc.getZ()+mod.getModZ());
                ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(loc);
	            if ( ids.size() > 0 ) {
	                if ( !iCoLand.hasPermission(player, "bypass") && !iCoLand.landMgr.canBuildDestroy(player, loc) ) {
	                    event.setCancelled(true);
	                    Messaging mess = new Messaging((CommandSender)player);
	                    mess.send("{ERR}You can't do that here.");
	                }
	            }
	        }
	    }
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
	    Player player = event.getPlayer();

	    if ( !checkNow(player) ) return;

        String playerName = player.getName();
        
        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(player.getLocation());
        Integer landId = (ids.size()>0)?ids.get(0):0;
	    
	    if (!locMap.containsKey(playerName)) locMap.put(playerName, landId) ;
	    
	    int idFrom = locMap.get(playerName);
		int idTo = landId;
		
		if ( idTo == 0 ) lastNonLandLoc.put(playerName, player.getLocation());
		
		locMap.put(playerName, idTo);

        Land landFrom = iCoLand.landMgr.getLandById(idFrom);
        Land landTo = iCoLand.landMgr.getLandById(idTo);
        if ( idFrom != 0 && landFrom == null ) {
            locMap.put(playerName, 0);
            idFrom = 0;
        }
        
		if ( Config.addonsEnabled.get("noenter") && 
		     landTo != null && idTo != 0 && landTo.hasAddon("noenter") && !landTo.hasPermission(playerName) && 
		     !iCoLand.hasPermission(player, "bypass") ) {
            Location loc = lastNonLandLoc.get(playerName);
            if ( loc != null ) {
                locMap.put(playerName, 0);
                player.sendMessage("Can't enter this land");
                player.teleport(loc);
            }
		} else if ( Config.addonsEnabled.get("noenter") && idFrom != idTo ) {
		    if ( idFrom != 0 ) {
		            if ( landFrom.hasAddon("announce") ) {
		            player.sendMessage("Leaving "+(landFrom.locationName.isEmpty()?"unnamed land":landFrom.locationName));
		        }
		    }
		    if ( idTo != 0 ) {
                if ( landTo.hasAddon("announce") ) {
                    player.sendMessage("Entering "+(landTo.locationName.isEmpty()?"unnamed land":landTo.locationName));
                }
		    }
		}
		
	}
	
	public boolean checkNow(Player player) {
	    String playerName = player.getName();
	    Long now = System.currentTimeMillis();
	    
        if ( (!timeMap.containsKey(playerName)) 
                || (now > timeMap.get(playerName)) ) {
            timeMap.put(playerName, System.currentTimeMillis() + Config.announceCheckInterval);
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
package me.slaps.iConomyLand;

import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;


public class iConomyLandPlayerListener extends PlayerListener {
	
    private static HashMap<String, Long> timeMap;
    private static HashMap<String, Integer> locMap;
    private static int checkDelay = 200; // milliseconds
    
    
	public iConomyLandPlayerListener(iConomyLand plug) {
	    plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Monitor, plug);
	    if ( iConomyLand.debugMode )
	        plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Priority.Highest, plug);
	    
	    timeMap = new HashMap<String, Long>();
	    locMap = new HashMap<String, Integer>();
	}

    @Override
    public void onPlayerCommandPreprocess ( PlayerChatEvent event ) {
        if (iConomyLand.debugMode)
            iConomyLand.info("iConomyLandPlayerListener.onPlayerCommandPreprocess(): Player: " + 
                           event.getPlayer().getName() + " msg: " + event.getMessage() + 
                           " Canceled? " + ( event.isCancelled()? "Yes": "No" ) );
       
    }	
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
	    Player player = event.getPlayer();

	    if ( !checkNow(player) ) return;

        String playerName = player.getName();
        
        /*
	    DecimalFormat df = new DecimalFormat("#.##");
	    if ( iConomyLand.debugMode ) iConomyLand.info("onPlayerMove: ("+
	            df.format(event.getFrom().getX())+","+
	            df.format(event.getFrom().getY())+","+
	            df.format(event.getFrom().getZ())+") to ("+
	            df.format(event.getTo().getX())+","+
	            df.format(event.getTo().getY())+","+
	            df.format(event.getTo().getZ())+")"+
	            (event.getFrom().getBlock().equals(event.getTo().getBlock())?"":"(NEW BLOCK)")
	            );
        */
	    
	    if (!locMap.containsKey(playerName)) locMap.put(playerName, iConomyLand.landMgr.getLandID(player.getLocation()) );
	    
	    int locFrom = locMap.get(playerName);
		int locTo = iConomyLand.landMgr.getLandID(player.getLocation());
		
		locMap.put(playerName, locTo);
		
		if ( locFrom != locTo ) {
		    if ( locFrom != 0 ) {
		        if ( iConomyLand.landMgr.getLandById(locFrom).hasAddon("announce") ) {
		            player.sendMessage("Leaving ...");
		        }
		    }
		    if ( locTo != 0 ) {
                if ( iConomyLand.landMgr.getLandById(locTo).hasAddon("announce") ) {
                    player.sendMessage("Entering ...");
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


    
    

}
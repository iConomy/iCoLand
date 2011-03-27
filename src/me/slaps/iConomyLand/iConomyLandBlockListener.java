package me.slaps.iConomyLand;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;

public class iConomyLandBlockListener extends BlockListener {
	
	public iConomyLandBlockListener(iConomyLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED, this, Priority.Monitor, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Priority.High, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, this, Priority.High, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, this, Priority.High, plug);
		 	
	}
	
	@Override
	public void onBlockRightClick(BlockRightClickEvent event) {
	    Player player = event.getPlayer();
	    String playerName = player.getName();
	    if ( iConomyLand.cmdMap.containsKey(playerName) && iConomyLand.cmdMap.get(playerName).equals("select") ) {
	        Cuboid newCuboid;
	        Location loc = event.getBlock().getLocation();
	        
	        if ( iConomyLand.tmpCuboidMap.containsKey(playerName) ) {
	            newCuboid = iConomyLand.tmpCuboidMap.get(playerName);
	            newCuboid.setLocation(loc);
	        } else {
	            newCuboid = new Cuboid(loc); 
	        }
	        
            Messaging mess = new Messaging((CommandSender)player);
	        
	        if ( newCuboid.isValid() ) {
	            mess.send("{}Land selected!");
	            iConomyLand.landMgr.showSelectLandInfo(player, newCuboid);
	            iConomyLand.cmdMap.remove(playerName);
	        } else {
                iConomyLand.tmpCuboidMap.put(playerName, newCuboid);
	            mess.send("{}Select 2nd corner");
	        }
	    }
	}
	
	public void onBlockBreak( BlockBreakEvent event ) {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        if ( !iConomyLand.landMgr.canBuild(player.getName(), loc) ) {
            event.setCancelled(true);
            Messaging mess = new Messaging((CommandSender)player);
            mess.send("{ERR}You can't do that here.");
        }
	}
	
	public void onBlockPlace( BlockPlaceEvent event )    {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        if ( !iConomyLand.landMgr.canBuild(player.getName(), loc) ) {
            event.setCancelled(true);
            Messaging mess = new Messaging((CommandSender)player);
            mess.send("{ERR}You can't do that here.");
        }
	}
	
	public void onBlockIgnite( BlockIgniteEvent event )  {
	    
	    if ( event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) ) {
            Location loc = event.getBlock().getLocation();
            Player player = event.getPlayer();
            if ( !iConomyLand.landMgr.canBuild(player.getName(), loc) ) {
                event.setCancelled(true);
                Messaging mess = new Messaging((CommandSender)player);
                mess.send("{ERR}You can't do that here.");
            }
	    }
	}
	


}

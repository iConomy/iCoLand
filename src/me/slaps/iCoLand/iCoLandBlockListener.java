package me.slaps.iCoLand;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class iCoLandBlockListener extends BlockListener {
	
	public iCoLandBlockListener(iCoLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BURN, this, Priority.Low, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, this, Priority.Low, plug);
	}
	
	public void onBlockBreak( BlockBreakEvent event ) {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        if ( !iCoLand.landMgr.canBuildDestroy(player, loc) && !iCoLand.hasPermission(player, "bypass") ) {
            event.setCancelled(true);
            Messaging mess = new Messaging((CommandSender)player);
            mess.send("{ERR}You can't do that here.");
        }
	}
	
	public void onBlockPlace( BlockPlaceEvent event )    {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        if ( !iCoLand.landMgr.canBuildDestroy(player, loc) && !iCoLand.hasPermission(player, "bypass") ) {
            event.setCancelled(true);
            Messaging mess = new Messaging((CommandSender)player);
            mess.send("{ERR}You can't do that here.");
        }
	}
	
	public void onBlockIgnite( BlockIgniteEvent event )  {
	    ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getBlock().getLocation());
	    for(Integer id : ids) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nofire") ) {
                event.setCancelled(true);
            }
	    }
	    
	    if ( ids.size() == 0 && !Config.unclaimedLandCanBurn ) {
            event.setCancelled(true);
            if ( event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) ) {
                Messaging mess = (new Messaging(event.getPlayer()));
                mess.send("{ERR}You can't do that here.");
            }
        }
	}
	
    public void onBlockBurn( BlockBurnEvent event )  {
        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getBlock().getLocation());
        for(Integer id : ids) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nofire") ) {
                event.setCancelled(true);
            }
        }
        
        if ( ids.size() == 0 && !Config.unclaimedLandCanBurn ) {
            event.setCancelled(true);
        }
    }
    
    public void onBlockFromTo( BlockFromToEvent event ) {
        ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(event.getToBlock().getLocation());
        for(Integer id : ids) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("noflow") ) {
                event.setCancelled(true);
            }
        }
    }

}

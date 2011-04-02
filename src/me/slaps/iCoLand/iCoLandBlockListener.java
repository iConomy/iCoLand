package me.slaps.iCoLand;

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
        if ( !iCoLand.landMgr.canBuild(player.getName(), loc) ) {
            event.setCancelled(true);
            Messaging mess = new Messaging((CommandSender)player);
            mess.send("{ERR}You can't do that here.");
        }
	}
	
	public void onBlockPlace( BlockPlaceEvent event )    {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        if ( !iCoLand.landMgr.canBuild(player.getName(), loc) ) {
            event.setCancelled(true);
            Messaging mess = new Messaging((CommandSender)player);
            mess.send("{ERR}You can't do that here.");
        }
	}
	
	public void onBlockIgnite( BlockIgniteEvent event )  {
	    if ( event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) ) {
            Location loc = event.getBlock().getLocation();
            Player player = event.getPlayer();
            if ( !iCoLand.landMgr.canBuild(player.getName(), loc) ) {
                event.setCancelled(true);
                Messaging mess = new Messaging((CommandSender)player);
                mess.send("{ERR}You can't do that here.");
            }
	    }
        Integer id = iCoLand.landMgr.getLandId(event.getBlock().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nofire") ) {
                event.setCancelled(true);
            }
        }
	}
	
    public void onBlockBurn( BlockBurnEvent event )  {
        Integer id = iCoLand.landMgr.getLandId(event.getBlock().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("nofire") ) {
                event.setCancelled(true);
            }
        }
    }
    
    public void onBlockFromTo( BlockFromToEvent event ) {
        Integer id = iCoLand.landMgr.getLandId(event.getToBlock().getLocation());
        if ( id > 0 ) {
            if ( iCoLand.landMgr.getLandById(id).hasAddon("noflow") ) {
                event.setCancelled(true);
            }
        }
    }

}

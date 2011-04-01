package me.slaps.iCoLand;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class iCoLandBlockListener extends BlockListener {
	
	public iCoLandBlockListener(iCoLand plug) {
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Priority.High, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, this, Priority.High, plug);
        plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, this, Priority.High, plug);
		 	
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
	}
	


}

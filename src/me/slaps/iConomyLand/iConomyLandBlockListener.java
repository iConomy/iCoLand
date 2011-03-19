package me.slaps.iConomyLand;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;

public class iConomyLandBlockListener extends BlockListener {
	
	public iConomyLandBlockListener(iConomyLand plug) {
		plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED, this, Priority.Monitor, plug);
		
	}
	
	@Override
	public void onBlockRightClick(BlockRightClickEvent event) {
	}
	


}

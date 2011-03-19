package me.slaps.DMWrapper;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;

public class DMWrapperBlockListener extends BlockListener {
	
	public DMWrapperBlockListener(DMWrapper plug) {
		plug.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED, this, Priority.Monitor, plug);
		
	}
	
	@Override
	public void onBlockRightClick(BlockRightClickEvent event) {
		Player player = event.getPlayer();
		String pname = player.getName();
		String cmd = DMWrapper.cmdMap.get(pname);

		if ( cmd == null ) return;
		
		Block b = event.getBlock();
		
		if ( cmd.equals("location set") ) {
			player.sendMessage("set corner 1 to: "+b.getX()+","+b.getY()+","+b.getZ());
			player.sendMessage("please right click the 2nd corner");
			DMWrapper.tmpShop.put(pname, new ShopLocation(new Location(player.getWorld(), b.getX(), b.getY(), b.getZ())));
			DMWrapper.cmdMap.put(pname, "location set 2");
		} else if ( cmd.equals("location set 2") ) {
			player.sendMessage("set corner 2 to: "+b.getX()+","+b.getY()+","+b.getZ());
			ShopLocation tmp = DMWrapper.tmpShop.get(pname);
			DMWrapper.tmpShop.remove(pname);
			if ( tmp == null ) {
				player.sendMessage("Error creating shop location - 1");
			} else {
				tmp.setLocation(2, new Location(player.getWorld(), b.getX(), b.getY(), b.getZ()));
				if ( tmp.set ) {
					if ( DMWrapper.locMgr.add(tmp) ) {
						player.sendMessage("Shop location created!");
					} else {
						player.sendMessage("Intersects existing shop location!");
					}
				}
				else player.sendMessage("Error creating shop location - 2");
			}
			DMWrapper.cmdMap.remove(pname);
		}
	}
	


}

package me.slaps.iCoLand;

import org.bukkit.entity.Player;

public class HealTask implements Runnable {
    
    public void run() {
        Player[] players = iCoLand.server.getOnlinePlayers();
        for(Player player : players) {
            Integer id = iCoLand.landMgr.getLandId(player.getLocation());
            if ( id > 0 ) {
                if ( iCoLand.landMgr.getLandById(id).hasAddon("heal") ) {
                    if ( player.getHealth() < 20 ) { 
                        player.setHealth(player.getHealth() + 1);
                    }
                }
            }
        }
    }
}

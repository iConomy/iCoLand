package me.slaps.iCoLand;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class TaskLandHeal implements Runnable {
    
    public void run() {
        Player[] players = iCoLand.server.getOnlinePlayers();
        for(Player player : players) {
            ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(player.getLocation());
            for(Integer id : ids) {
                if ( iCoLand.landMgr.getLandById(id).hasAddon("heal") ) {
                    if ( player.getHealth() < 20 ) { 
                        player.setHealth(player.getHealth() + 1);
                    }
                }
            }
        }
    }
}

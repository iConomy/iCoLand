package me.slaps.iCoLand;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;


public class TaxTask implements Runnable {
    
    public void run() {
        Player[] players = iCoLand.server.getOnlinePlayers();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp timeThreshold = new Timestamp(System.currentTimeMillis()-Config.taxTimeMinutes*60*1000);

        ArrayList<Land> lands = iCoLand.landMgr.listLandPastTaxTime(timeThreshold);

        if ( Config.debugMode ) 
            iCoLand.info("Starting tax task...  "+now);
        
        for(Land land : lands) {
                double tax = land.location.volume()*Config.taxRate;
                
                Account acc = iConomy.getBank().getAccount(land.owner);
                
                if ( acc == null ) {
                    iCoLand.info("Land ID# "+land.getID()+ " belongs to "+land.owner+", but he does not have an iConomy account!");
                } else if ( acc.hasEnough(tax) ) {
                    if (!iCoLand.landMgr.updateTaxTime(land.getID(), now)) {
                        iCoLand.severe("Error updating tax timestamp on land ID# "+land.getID());
                    }
                    
                    // subtract tax out
                    acc.subtract(tax);
                    
                    int i = playerInList(players, land.owner);
                    if ( i > -1 ) {
                        Messaging mess = new Messaging(players[i]);
                        mess.send("{}Land ID# {PRM}"+land.getID()+" {}taxed for {PRM}"+iCoLand.df.format(tax));
                    } 
                    
                    if ( Config.debugMode ) 
                        iCoLand.info("Land ID# "+land.getID()+" taxed for "+iCoLand.df.format(tax));
                } else {
                    // not enough for taxes, delete zone!
                    if (!iCoLand.landMgr.removeLandById(land.getID())) {
                        iCoLand.severe("Error removing land ID# "+land.getID()+" due to unpaid taxes");
                    }
                    
                    int i = playerInList(players, land.owner);
                    if ( i > 0 ) {
                        Messaging mess = new Messaging(players[i]);
                        mess.send("{ERR}Not enough money to pay tax of {PRM}"+tax+" on land ID# {PRM}"+land.getID());
                    } 
                    if ( Config.debugMode ) 
                        iCoLand.info("Not enough money to pay tax of "+tax+" on land ID# "+land.getID());
                }
                
        }
    }
    
    public int playerInList(Player[] players, String playerName) {
        for(int i=0;i<players.length;i++) {
            if ( players[i].getName().equals(playerName) ) {
                return i;
            }
        }
        return -1;
    }
}

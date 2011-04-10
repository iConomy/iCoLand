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

        if ( Config.debugMode1 ) 
            iCoLand.info("Starting tax task...  "+now);
        
        for(Land land : lands) {
            double tax = land.location.volume()*Config.taxRate;
            
            Account acc = iConomy.getBank().getAccount(land.owner);
            Account bank = iConomy.getBank().getAccount(Config.bankName);
            
            if ( acc == null ) {
                iCoLand.info("Land ID# "+land.getID()+ " belongs to "+land.owner+", but he does not have an iConomy account!");
            } else if ( acc.hasEnough(tax) ) {
                if (!iCoLand.landMgr.updateTaxTime(land.getID(), now)) {
                    iCoLand.severe("Error updating tax timestamp on land ID# "+land.getID());
                }
                
                if ( !iCoLand.hasPermission(land.location.setLoc1.getWorld().getName(), land.owner, "notax") ) {
                    if ( acc.hasEnough(tax) ) {
                        // subtract tax out
                        acc.subtract(tax);
                        bank.add(tax);
                        
                        int i = playerInList(players, land.owner);
                        if ( i > -1 ) {
                            Messaging mess = new Messaging(players[i]);
                            mess.send("{}Land ID# {PRM}"+land.getID()+" {}taxed for {PRM}"+iCoLand.df.format(tax));
                        } 
                        
                        if ( Config.debugMode ) 
                            iCoLand.info(land.owner+ " - Land ID# "+land.getID()+" taxed for "+iCoLand.df.format(tax));
                    } else {
                        // not enough for taxes, delete zone/mark inactive !
                        if (!iCoLand.landMgr.updateActive(land.getID(), false)) {
                            iCoLand.severe("Error setting land ID# "+land.getID()+" inactive, due to unpaid taxes");
                        }
    
                        Double price = Double.valueOf(iCoLand.df.format(land.getTotalPrice()));
                        if ( !iCoLand.hasPermission(land.location.setLoc1.getWorld().getName(), land.owner, "notax" ) ) {
                            acc.add(price);
                            bank.subtract(price);
                        }
                        
                        int i = playerInList(players, land.owner);
                        if ( i > 0 ) {
                            Messaging mess = new Messaging(players[i]);
                            mess.send("{ERR}Not enough money to pay tax of {PRM}"+tax+" on land ID# {PRM}"+land.getID());
                            if ( !iCoLand.hasPermission(land.location.setLoc1.getWorld().getName(), land.owner, "notax" ) ) {
                                mess.send("{}Sold land ID# {PRM}"+land.getID()+"{} for {PRM}"+price);
                            } else {
                                mess.send("{}Sold land ID# {PRM}"+land.getID()+"{} for {PRM}0 {BKT}({PRM}"+price+"{BKT})");
                            }
                        } 
                        if ( Config.debugMode ) {
                            iCoLand.info(land.owner+" didn't have enough money to pay tax of "+tax+" on land ID# "+land.getID());
                            iCoLand.info("{}Sold land ID# {PRM}"+land.getID()+"{} for {PRM}0 {BKT}({PRM}"+price+"{BKT})");
                            if ( !iCoLand.hasPermission(land.location.setLoc1.getWorld().getName(), land.owner, "notax" ) ) {
                                iCoLand.info("Sold land ID# "+land.getID()+" for "+price);
                            } else {
                                iCoLand.info("Sold land ID# "+land.getID()+" for 0 ("+price+")");
                            }
                        }
                    }
                } else {
                    // notax perm
                    int i = playerInList(players, land.owner);
                    if ( i > -1 ) {
                        Messaging mess = new Messaging(players[i]);
                        mess.send("{}Land ID# {PRM}"+land.getID()+" {}taxed for {PRM}0 {BKT}({PRM}"+iCoLand.df.format(tax)+"{BKT})");
                    } 
                    
                    if ( Config.debugMode ) 
                        iCoLand.info(land.owner+ " - Land ID# "+land.getID()+" taxed for 0 ("+iCoLand.df.format(tax)+") - notax perm");
                }
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

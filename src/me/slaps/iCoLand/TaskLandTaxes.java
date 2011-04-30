package me.slaps.iCoLand;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;


public class TaskLandTaxes implements Runnable {
    
    private static iCoLand ic;
    private static int taskID;
    private boolean main;
    private boolean taxes;
    
    private HashMap<Integer, Timestamp> updateList;
    
    TaskLandTaxes(iCoLand ic) {
        taskID = -1;
        this.ic = ic;
        main = true;
        taxes = true;
    }
    
    TaskLandTaxes(boolean taxes, HashMap<Integer,Timestamp>updateList) {
        this.updateList = updateList;
        this.taxes = taxes;
    }
    
    public void run() {
        if ( main ) {
            Player[] players = iCoLand.server.getOnlinePlayers();
            int timeOffset = Config.taxTimeMinutes*60*1000;
            Timestamp now = new Timestamp(System.currentTimeMillis());
    
            if ( Config.debugMode1 ) 
                iCoLand.info("Time offset ms: "+timeOffset);
            
            ArrayList<Land> lands = iCoLand.landMgr.listLandPastTaxTime(now);
    
            if ( taskID >= 0 && (iCoLand.server.getScheduler().isCurrentlyRunning(taskID) || iCoLand.server.getScheduler().isQueued(taskID)) )
                return;
            else if ( taskID > 0 ) 
                taskID = -1;
            
            
            if ( Config.debugMode1 )
                iCoLand.info("Starting tax task...  "+now);
            
            for(Land land : lands) {
                double tax = Double.valueOf(iCoLand.df.format(land.location.volume()*Config.pricePerBlock.get("raw")*Config.taxRate));
                
                Account acc = iConomy.getBank().getAccount(land.owner);
                Account bank = iConomy.getBank().getAccount(Config.bankName);
                
                if ( Config.debugMode1 ) 
                    iCoLand.info("Last tax date for ID#"+land.getID()+" : "+land.dateTax);
                
                Timestamp nextTaxDate = new Timestamp(land.dateTax.getTime());
                HashMap<Integer, Timestamp> updates = new HashMap<Integer, Timestamp>();
                while ( nextTaxDate.before(now) ) {
                    nextTaxDate.setTime(nextTaxDate.getTime()+timeOffset);
                    if ( acc == null ) {
                        iCoLand.info("Land ID# "+land.getID()+ " belongs to "+land.owner+", but he does not have an iConomy account!");
                    } else if  ( !iCoLand.hasPermission(land.location.setLoc1.getWorld().getName(), land.owner, "admin.notax") ) {
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
                            
                            int i = playerInList(players, land.owner);
                            if ( i >= 0 ) {
                                Messaging mess = new Messaging(players[i]);
                                mess.send("{ERR}Not enough money to pay tax of {PRM}"+tax+" on land ID# {PRM}"+land.getID());
                                mess.send("{}Land ID# {PRM}"+land.getID()+"{} marked inactive");
                            }
        
                            iCoLand.info(land.owner+" didn't have enough money to pay tax of "+tax+" on land ID# "+land.getID());
                            iCoLand.info("Land ID# "+land.getID()+" marked inactive");
                        }
                        
                    } else {
                        // notax perm
                        int i = playerInList(players, land.owner);
                        if ( i > -1 ) {
                            Messaging mess = new Messaging(players[i]);
                            mess.send("{}Land ID# {PRM}"+land.getID()+" {}taxed for {PRM}0 {BKT}({PRM}"+iCoLand.df.format(tax)+"{BKT})");
                        }
                        
                        iCoLand.info(land.owner+ " - Land ID# "+land.getID()+" taxed for 0 ("+iCoLand.df.format(tax)+") - notax perm");
                    }
                    
                    updates.put(land.getID(), nextTaxDate);
    
                }
                
                // TODO
                taskID = iCoLand.server.getScheduler().scheduleAsyncDelayedTask(ic, new TaskLandTaxes(true, updates));
                updates = null;
    
            }
            
    
    
            Timestamp timeThreshold = new Timestamp(now.getTime()-Config.inactiveDeleteTime*60*1000);
            lands = iCoLand.landMgr.listLandPastInactiveTime(timeThreshold);
    
            if ( Config.debugMode1 )
                iCoLand.info("Starting inactive deletion task...  "+now);
            
            for(Land land : lands) {
                Account acc = iConomy.getBank().getAccount(land.owner);
                Account bank = iConomy.getBank().getAccount(Config.bankName);
                double tax = Double.valueOf(iCoLand.df.format(land.location.volume()*Config.pricePerBlock.get("raw")*Config.taxRate));
    
                if ( iCoLand.landMgr.removeLandById(land.getID()) ) {
    
                    // find if player is online
                    int i = playerInList(players, land.owner);
                    if ( i > -1 ) {
                        Messaging mess = new Messaging(players[i]);
                        mess.send("{}Land ID# {PRM}"+land.getID()+" {}deleted due to inactivity");
                    } 
                    
                    Double price = Double.valueOf(iCoLand.df.format(land.getTotalPrice()));
                    if ( !iCoLand.hasPermission(land.location.setLoc1.getWorld().getName(), land.owner, "admin.notax" ) ) {
                        acc.add(price-tax);
                        bank.subtract(price-tax);
                    }
    
                    i = playerInList(players, land.owner);
                    if ( i >= 0 ) {
                        Messaging mess = new Messaging(players[i]);
                        mess.send("{}Land ID# {PRM}"+land.getID()+"{} sold for {PRM}"+(price-tax));
                    } 
                    
    //                if ( Config.debugMode )
                        iCoLand.info("Land ID# "+land.getID()+" ("+land.owner+") sold for "+(price-tax));
                    
                } else {
                    iCoLand.info("Problem removing Land ID# "+land.getID());
                }
                
                
    
            }
        } else {
            if ( taxes ) {
                for ( Integer id : updateList.keySet() ) {
                    
                    if ( !iCoLand.landMgr.updateTaxTime(id, updateList.get(id)) ) {
                        iCoLand.severe("Error updating tax timestamp on land ID# "+id);
                    }
                    
                    if ( Config.debugMode1 ) 
                        iCoLand.info("New tax date for ID#"+id+" : "+updateList.get(id) );
                    try {
                        Thread.sleep(100);
                    } catch ( Exception ex ) {
                        ex.printStackTrace();
                    }
                }
            } else {
                
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

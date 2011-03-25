package me.slaps.iConomyLand;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class iConomyLandCommandListener implements CommandExecutor {
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Messaging mess = new Messaging(sender);

        if ( iConomyLand.debugMode ) {
            String debug = "iConomyLand.onCommand(): " + ((sender instanceof Player) ? "Player " + ((Player) sender).getName() : "Console") + " Command " + cmd.getName() + " args: ";
            for (int i = 0; i < args.length; i++) 
                debug += args[i] + " ";
            iConomyLand.info(debug);
        }

        // temporary
        if (!(sender instanceof Player))
        	return false;

        // is our command?
        if ( Misc.isAny(cmd.getName(), "icl", "iConomyLand", "iConomyLand:icl", "iConomyLand:iConomyLand") ) {
            if (iConomyLand.debugMode) iConomyLand.info("Is an /icl or /iConomyLand command");

            // /icl
            if ( args.length == 0 ) {
                showHelp(sender, "");
                return true;
                
            // /icl help
            }  else if ( args[0].equalsIgnoreCase("help") ) {
                if ( args.length == 1 ) {
                    showHelp(sender, "");
                } else {
                    showHelp(sender, args[1] );
                }
                return true;
                
            // /icl list
            } else if (args[0].equalsIgnoreCase("list") ) {
                if ( iConomyLand.hasPermission(sender, "list") ) { 
                    showList(sender);
                } else {
                    mess.send("{ERR}No access for list");
                }
                return true;
                
            // /icl select
            } else if (args[0].equalsIgnoreCase("select") ) {
                if ( iConomyLand.hasPermission(sender, "select") ) {
                    if ( !(sender instanceof Player) ) {
                        mess.send("Console can't select");
                    } else if ( args.length == 1 ) {
                        selectArea((Player)sender);
                    } else if ( args.length == 2 & args[0].equalsIgnoreCase("cancel") ) {
                        mess.send("{}Cancelling current selection.");
                        iConomyLand.cmdMap.remove(((Player)sender).getName());
                        iConomyLand.tmpCuboidMap.remove(((Player)sender).getName());                        
                    } else {
                        mess.send("{ERR}Too many arguments.");
                        showHelp(sender, "select");
                    }
                } else {
                    mess.send("{ERR}No access for that...");                    
                }
                return true;
                
            // /icl info
            } else if (args[0].equalsIgnoreCase("info") ) {
                if ( iConomyLand.hasPermission(sender, "info") ) {
                    if ( args.length == 1 ) {
                        if ( sender instanceof Player )
                            showLandInfo((Player)sender, "");
                        else
                            mess.send("{ERR}Console needs to supply arguments for this command");
                    } else {
                        showLandInfo(sender, args[1]);
                    }
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;
                
            // /icl buy
            } else if (args[0].equalsIgnoreCase("buy") ) {
                if ( iConomyLand.hasPermission(sender, "buy") ) { 
                    buyLand(sender);
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;

            // /icl sell
            } else if (args[0].equalsIgnoreCase("sell") ) {
                if ( iConomyLand.hasPermission(sender, "sell") ) { 
                    sellLand(sender);
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;

            // /icl modify <id> <perms|addons> <add|del> <tags>
            } else if (args[0].equalsIgnoreCase("modify") ) {
                if ( iConomyLand.hasPermission(sender, "modify") ) {
                    if ( args.length < 5 ) {
                        mess.send("{ERR}Not enough arguments");
                        showHelp(sender, "modify");
                    } else {
                        Integer id;
                        try { id = Integer.parseInt(args[1]); } catch (NumberFormatException e) { id = -1; }
                        if ( id <= 0 ) {
                            mess.send("{ERR}Bad ID");
                            showHelp(sender, "modify");
                        } else {
                            String tags = args[4];
                            for(int i=5;i<args.length;i++) tags += " "+args[i];
                            
                            if ( args[2].equals("perms") && iConomyLand.hasPermission(sender,"modify.perms") ) {
                                if ( args[3].equalsIgnoreCase("add") ) {
                                    addPerms(sender, id, tags);
                                } else if ( args[3].equalsIgnoreCase("del") ) {
                                    delPerms(sender, id, tags);
                                } else {
                                    mess.send("{ERR}Bad modifier - must be add or del");
                                }
                            } else if ( args[2].equals("addons") && iConomyLand.hasPermission(sender,"modify.addons") ) {
                                if ( args[3].equalsIgnoreCase("add") ) {
                                    addAddon(sender, id, tags);
                                } else if ( args[3].equalsIgnoreCase("del") ) {
                                    delAddon(sender, id, tags);
                                } else {
                                    mess.send("{ERR}Bad modifier - must be add or del");
                                }
                            } else if ( args[2].equals("owner") && iConomyLand.hasPermission(sender,"modify.owner") ) {
                                changeOwner(sender, id, tags);
                            } else {
                                mess.send("{ERR}Bad category");
                                showHelp(sender, "modify");
                            }
                        }
                    }
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;
                
            // unrecognized /icl command
            } else {
                mess.send("{}Unrecognized/invalid/malformed command!");
                mess.send("{} Please use {CMD}/icl help {BKT}[{PRM}topic{BKT}] {}for help");
                return true;
            }

        // command not recognized ( not /icl )
        } else {
            return false;
        }

    }
    
    public void showList(CommandSender sender) {
        Collection<Land> list;
        if( sender instanceof Player )
            list = iConomyLand.landMgr.getLandsOwnedBy(((Player)sender).getName());
        else
            list = iConomyLand.landMgr.getAllLands();
                
        Messaging mess = new Messaging(sender);
        Iterator<Land> itr = list.iterator();
        while(itr.hasNext()) {
            Land land = itr.next();
            mess.send("{PRM}ID#{}"+land.getID()+
                      " {PRM}V:{}"+land.location.volume()+
                      "{PRM}[{}"+land.location.toDimString()+
                      "{PRM}] C{}"+land.location.toCenterCoords()+
                      " {PRM}Ad:{}"+Land.writeAddonTags(land.addons)+
                      " {PRM}P:{}"+Land.writePermTags(land.canBuildDestroy)
                      );
        }
        
    }
    
    public void addPerms(CommandSender sender, Integer id, String tags) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandByID(id);
        String playerName = (sender instanceof Player)? ((Player)sender).getName():""; 

        if ( (sender instanceof Player) && !land.owner.equals(playerName) ) {
            mess.send("{ERR}No permission to do that");
        } else {
            HashMap<String, Boolean> parsedTags = Land.parsePermTags(tags);
            Set<String> keys = parsedTags.keySet();
            for( String key : keys ) {
                land.addPermission(key, parsedTags.get(key) );
            }
        }
        iConomyLand.landMgr.save();
    }
    
    public void delPerms(CommandSender sender, Integer id, String tags) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandByID(id);
        String playerName = (sender instanceof Player)? ((Player)sender).getName():""; 
        
        if ( (sender instanceof Player) && !land.owner.equals(playerName) ) {
            mess.send("{ERR}No permission to do that");
        } else {
            String[] split = tags.split(" ");
            for( String key : split ) {
                land.delPermission(key);
            }
        }
        iConomyLand.landMgr.save();
    }
    
    public void addAddon(CommandSender sender, Integer id, String tags) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandByID(id);
        String playerName = (sender instanceof Player)? ((Player)sender).getName():"";
        
        if ( (sender instanceof Player) && !land.owner.equals(playerName) ) {
            mess.send("{ERR}No permission to do that");
        } else {
            String[] split = tags.split(" ");
            for( String key : split ) {
                land.giveAddon(key);
            }
        }
        iConomyLand.landMgr.save();        
    }

    public void delAddon(CommandSender sender, Integer id, String tags) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandByID(id);
        String playerName = (sender instanceof Player)? ((Player)sender).getName():"";
        
        if ( (sender instanceof Player) && !land.owner.equals(playerName) ) {
            mess.send("{ERR}No permission to do that");
        } else {
            String[] split = tags.split(" ");
            for( String key : split ) {
                land.removeAddon(key);
            }
        }
        iConomyLand.landMgr.save();
    }
    
    public void changeOwner(CommandSender sender, Integer id, String tags) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandByID(id);
        String playerName = (sender instanceof Player)? ((Player)sender).getName():"";

        if ( (sender instanceof Player) && !land.owner.equals(playerName) ) {
            mess.send("{ERR}No permission to do that");
        } else {
            land.owner = tags;
        }
        
        iConomyLand.landMgr.save();
    }

    

    public void buyLand(CommandSender sender) {
        Messaging mess = new Messaging(sender);
        if ( sender instanceof Player ) {
            String playerName = ((Player)sender).getName();
            if ( iConomyLand.tmpCuboidMap.containsKey(playerName) ) {
                Cuboid newCuboid = iConomyLand.tmpCuboidMap.get(playerName);
                if ( newCuboid.isValid() ) {
                    Account acc = iConomy.getBank().getAccount(playerName);
                    double price = iConomyLand.landMgr.getPrice(newCuboid);
                    if ( acc.getBalance() > price ) {
                        if ( iConomyLand.landMgr.addLand(newCuboid, playerName, "", "") ) {
                            acc.subtract(price);
                            iConomyLand.cmdMap.remove(playerName);
                            mess.send("{}Bought selected land for {PRM}"+price);
                            mess.send("{}Bank Balance: {PRM}"+acc.getBalance());
                        } else {
                            mess.send("{ERR}Error buying land");
                        } 
                    } else {
                        mess.send("{ERR}Not enough in account. Bank: "+acc.getBalance()+
                                  " Price: "+price); 
                    }
                } else {
                    mess.send("{ERR}Invalid selection");
                }
            } else {
                mess.send("{ERR}Nothing selected");
            }
        } else {
            mess.send("Console can't buy land");
        }
    }
    
    public void sellLand(CommandSender sender) {
        
    }
    
    
    public void showLandInfo(Player sender, String... args) {
        Messaging mess = new Messaging(sender);
        
        if ( args[0].isEmpty() ) {
            // use location search or selected search
            String playerName = sender.getName();
            if ( iConomyLand.tmpCuboidMap.containsKey(playerName) ) {
                Cuboid select = iConomyLand.tmpCuboidMap.get(playerName);
                if ( select.isValid() ) {
                    iConomyLand.landMgr.showSelectLandInfo((CommandSender)sender, select);
                } else {
                    mess.send("{ERR}Current selection invalid! Use {CMD}/lwc select{} to cancel");
                }
            } else {
                Location loc = sender.getLocation();
                Integer landid = iConomyLand.landMgr.getLandID(loc);
                if ( landid > 0 ) {
                    iConomyLand.landMgr.showSelectLandInfo((CommandSender)sender, landid);
                } else {
                    mess.send("{ERR}No current selection, not on owned land");
                }
            }
            
        } else {
            
        }
    }
    
    public void showLandInfo(CommandSender sender, String... args) {
        if ( args.length == 0 ) {
            showHelp(sender,"info");
        } else {
            Integer id;            
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e ) {
                id = -1;
            }
            if ( id > 0 ) {
                iConomyLand.landMgr.showSelectLandInfo((CommandSender)sender, id);
            } else {
                showHelp(sender,"info");
            }
        }
    }
    
    public void showHelp(CommandSender sender, String topic) {
        Messaging mess = new Messaging(sender);
        mess.send("{}"+Misc.headerify("{CMD} " + iConomyLand.name + " {BKT}({CMD}" + iConomyLand.codename + "{BKT}){}"));
    	if ( topic == null || topic.isEmpty() ) {
    	    
    	    mess.send(" {CMD}/icl {}- main command");
    	    mess.send(" {CMD}/icl {PRM}help {BKT}[{PRM}topic{BKT}] {}- help topics");
    	    String topics = "";
            if ( iConomyLand.hasPermission(sender, "list") ) topics += "list";
            if ( iConomyLand.hasPermission(sender, "select") ) topics += " select";
            if ( iConomyLand.hasPermission(sender, "info") ) topics += " info";
            if ( iConomyLand.hasPermission(sender, "buy") ) topics += " buy";
            if ( iConomyLand.hasPermission(sender, "modify") ) topics += " modify";
    	    
    	    mess.send(" {} help topics: {CMD}" + topics);
    	    
    	} else if ( topic.equalsIgnoreCase("list") ) {
            if ( iConomyLand.hasPermission(sender, "list") ) { 
                mess.send(" {CMD}/icl {PRM}list {}- list owned land");
            }
            
        } else if ( topic.equalsIgnoreCase("select") ) {
            if ( iConomyLand.hasPermission(sender, "select") ) { 
                mess.send(" {CMD}/icl {PRM}select {}- select land");
            }
        } else if ( topic.equalsIgnoreCase("info") ) {
            if ( iConomyLand.hasPermission(sender, "info") ) { 
                mess.send(" {CMD}/icl {PRM}info {}- get land info");
            }
            
        } else if ( topic.equalsIgnoreCase("buy") ) {
            if ( iConomyLand.hasPermission(sender, "buy") ) { 
                mess.send(" {CMD}/icl {PRM}buy {}- purchase selected land");
            }
            
        } else if ( topic.equalsIgnoreCase("modify") ) {
            if ( iConomyLand.hasPermission(sender, "modify") ) { 
                mess.send(" {CMD}/icl {PRM}modify {}- modify land settings");
            }
            
        }
    }
    
    public boolean selectArea(Player player) {
        String playerName = player.getName();
        Messaging mess = new Messaging((CommandSender)player);
        if ( iConomyLand.cmdMap.containsKey(playerName) ) {
            String action = iConomyLand.cmdMap.get(playerName);
            if ( action.equals("select") ) {
                mess.send("{}Cancelling current selection.");
                iConomyLand.cmdMap.remove(playerName);
                iConomyLand.tmpCuboidMap.remove(playerName);
            } else {
                mess.send("{ERR} Canceling "+action);
                iConomyLand.cmdMap.remove(playerName);
            }
        }
        mess.send("{}Select 1st Corner");
        iConomyLand.cmdMap.put(playerName,"select");
        return true;
    }
    

    
    
    
}

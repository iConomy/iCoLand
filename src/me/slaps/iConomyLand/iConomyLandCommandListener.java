package me.slaps.iConomyLand;

import java.util.Collection;
import java.util.Iterator;

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

        if ( Config.debugMode ) {
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
            if (Config.debugMode) iConomyLand.info("Is an /icl or /iConomyLand command");

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
                
            // /icl edit <LANDID> <name|perms> <tags>
            } else if (args[0].equalsIgnoreCase("edit") ) {
                if ( !(sender instanceof Player) ) {
                    mess.send("{ERR}Console can not edit with this command");
                } else if ( iConomyLand.hasPermission(sender, "edit") ) {
                    Player player = (Player)sender;
                    if ( args.length > 3 ) {
                        Integer id;
                        try { 
                            id = Integer.parseInt(args[1]); 
                        } catch (NumberFormatException ex) { 
                            mess.send("{ERR}Error reading <LANDID>");
                            return true;
                        }
                        if ( iConomyLand.landMgr.landIdExists(id) ) {
                            if ( iConomyLand.landMgr.isOwner(player.getName(), id) ) {                            
                                if ( Misc.isEither(args[2], "name", "perms") ) {
                                    String tags = "";
                                    for(int i=3;i<args.length;i++) tags += args[i] + " ";
                                    editLand(player, id, args[2], tags);
                                } else {
                                    mess.send("{ERR}Not a valid category");
                                    showHelp(sender,"edit");
                                }
                            } else {
                                mess.send("{ERR}Not owner of land ID# {PRM}"+id);
                            }
                        } else {
                            mess.send("{ERR}Land ID# {PRM}"+id+" {ERR}doesn't exist");
                        }
                    } else {
                        mess.send("{ERR}Too few arguments");
                    }
                } else {
                    mess.send("{ERR}No access for edit");
                }
                return true;
                
            // /icl modify <id> <perms|addons> <tags>
            } else if (args[0].equalsIgnoreCase("modify") ) {
                if ( iConomyLand.hasPermission(sender, "modify") ) {
                    if ( args.length < 4 ) {
                        mess.send("{ERR}Not enough arguments");
                        showHelp(sender, "modify");
                    } else {
                        Integer id;
                        try { 
                            id = Integer.parseInt(args[1]); 
                        } catch (NumberFormatException e) { 
                            mess.send("{ERR}Error parsing <LANDID>");
                            return true;
                        }
                        if ( !iConomyLand.landMgr.landIdExists(id) ) {
                            mess.send("{ERR}Land ID# {PRM}" + id + " {ERR}doesn't exist");
                        } else {
                            if ( Misc.isAny(args[2], "perms", "addons", "owner") ) {
                                String tags = args[3];
                                for(int i=4;i<args.length;i++) tags += args[i];
                                adminEditLand(sender, id, args[2], tags);
                            } else {
                                mess.send("{ERR}Bad category");
                            }
                        }
                    }
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;
                
            // /icl select
            } else if (args[0].equalsIgnoreCase("select") ) {
                if ( iConomyLand.hasPermission(sender, "select") ) {
                    if ( !(sender instanceof Player) ) {
                        mess.send("Console can't select");
                    } else if ( args.length == 1 ) {
                        selectArea((Player)sender);
                    } else if ( args.length == 2 & args[1].equalsIgnoreCase("cancel") ) {
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
                
            // /icl info [here|LANDID]
            } else if (args[0].equalsIgnoreCase("info") ) {
                if ( iConomyLand.hasPermission(sender, "info") ) {
                    if ( (args.length == 1) ) {
                        if ( sender instanceof Player )
                            showLandInfo(sender, "");
                        else
                            mess.send("{ERR}Console needs to supply arguments for this command");
                    } else if ( args.length == 2 ) {
                        showLandInfo(sender, args[1]);
                    } else {
                        mess.send("{ERR}Bad info command");
                        showHelp(sender, "info");
                    }
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;
                
            // /icl buy land
            // /icl buy addon <addon> <landID>
            } else if (args[0].equalsIgnoreCase("buy") ) {
                if ( !(sender instanceof Player) ) {
                    mess.send("{ERR}Console can't buy");
                } else if ( iConomyLand.hasPermission(sender, "buy") ) {
                    if ( args.length == 1 ) {
                        mess.send("{ERR}Not enough arguments");
                        showHelp(sender,"buy");
                    } else {
                        if ( args.length == 2 && args[1].equalsIgnoreCase("land") ) {
                            buyLand(sender);
                        } else if ( args.length > 2 && args[1].equalsIgnoreCase("addon") ) {
                            if ( args.length == 4 ) {
                                if ( Misc.isAny(args[2], "announce", "heal", "noenter") ) {
                                    try { 
                                        Integer id = Integer.parseInt(args[3]);
                                            if ( iConomyLand.landMgr.landIdExists(id) ) {
                                                buyAddon((Player)sender, args[2], id);
                                            } else {
                                                mess.send("{ERR}Land ID# "+id+" does not exist!");
                                            }
                                    } catch (NumberFormatException ex) {
                                        mess.send("{ERR}Error processing Land ID");
                                    }
                                } else {
                                    mess.send("{ERR}Not a valid addon");
                                }
                            } else {
                                mess.send("{ERR}Must specify which addon and land ID");
                            }
                        } else {
                            mess.send("{ERR}Bad buy command");
                            showHelp(sender, "buy");
                        }
                    }
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;

            // /icl sell land <ID>
            // /icl sell addon <ADDON> <ID>
            } else if (args[0].equalsIgnoreCase("sell") ) {
                if ( !(sender instanceof Player) ) {
                    mess.send("{ERR}Console can't sell land");
                } else if ( iConomyLand.hasPermission(sender, "sell") ){
                    if ( args.length == 3 && args[1].equalsIgnoreCase("land") ) {
                        Integer id = 0;
                        try {
                            id = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ex) {
                            mess.send("{ERR}Error parsing ID#");
                        }
                        
                        if ( iConomyLand.landMgr.landIdExists(id) ) {
                            sellLand((Player)sender, id);
                        } else {
                            mess.send("{ERR}Land ID# "+id+" does not exist");
                        }
                    } else if ( args.length == 4 && args[1].equalsIgnoreCase("addon") ) {
                        Integer id = 0;
                        try {
                            id = Integer.parseInt(args[3]);
                        } catch (NumberFormatException ex) {
                            mess.send("{ERR}Error parsing ID#");
                        }
                        
                        if ( iConomyLand.landMgr.landIdExists(id) ) {
                            if ( Misc.isAny(args[2], "announce", "healing", "noenter" ) ) {
                                sellAddon((Player)sender, args[2], id);
                            } else {
                                mess.send("{ERR}Not valid addon: {PRM}"+args[2]);
                            }
                        } else {
                            mess.send("{ERR}Land ID# "+id+" does not exist");
                        }
                    } else {
                        mess.send("{ERR}Bad sell command");
                        showHelp(sender,"sell");
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
    
    public void editLand(Player player, Integer id, String category, String args) {
        Messaging mess = new Messaging(player);
        Land land = iConomyLand.landMgr.getLandById(id);
        if ( category.equals("perms") ) {
            land.modifyBuildDestroyWithTags(args);
            mess.send("{}Permissions added");
            iConomyLand.landMgr.save();
        } else if ( category.equalsIgnoreCase("name") ) {
            land.locationName = args.substring(0, (args.length()>35)?35:args.length());
            iConomyLand.landMgr.save();
            mess.send("{}Location name changed");
        }
    }
    
    
    public void adminEditLand(CommandSender sender, Integer id, String category, String tags) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandById(id);
        if ( category.equals("perms") ) {
            editLand((Player)sender, id, category, tags);
        } else if ( category.equalsIgnoreCase("name") ) {
            editLand((Player)sender, id, category, tags);
        } else if ( category.equalsIgnoreCase("owner") ) {
            land.owner = tags;
            mess.send("{}Owner changed");
        } else if ( category.equalsIgnoreCase("addons") ) {
            mess.send("need to implement...");
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
 

    public void buyAddon(Player sender, String addon, Integer id) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandById(id);

        if ( land.owner.equalsIgnoreCase(sender.getName()) ) {

            Account acc = iConomy.getBank().getAccount(sender.getName());
            double price = Double.valueOf(iConomyLand.df.format(iConomyLand.landMgr.getLandById(id).getAddonPrice(addon)));
            
            if ( acc.getBalance() > price ) {
                acc.subtract(price);
                land.addAddon(addon);
                iConomyLand.landMgr.save();
                
                mess.send("{}Bought addon {PRM}"+addon+"{} for {PRM}"+iConomyLand.df.format(price));
                mess.send("{}Bank Balance: {PRM}"+iConomyLand.df.format(acc.getBalance()));
            } else {
                mess.send("{ERR}Not enough in account. Bank: "+iConomyLand.df.format(acc.getBalance())+
                        " Price: "+iConomyLand.df.format(price)); 
            }
        } else {
            mess.send("{ERR}Not owner of land ID# {PRM}"+id);
        }
    }
    
    public void sellAddon(Player sender, String addon, Integer id) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandById(id);
        
        if ( land.owner.equalsIgnoreCase(sender.getName()) ) {
        
            Account acc = iConomy.getBank().getAccount(sender.getName());
            double price = Double.valueOf(iConomyLand.df.format(land.getAddonPrice(addon)*Config.sellTax));
            
            acc.add(price);
            land.removeAddon(addon);
            iConomyLand.landMgr.save();
    
            mess.send("{}Sold addon {PRM}"+addon+" on land ID# {PRM}"+id+"{} for {PRM}"+price);
            mess.send("{}Bank Balance: {PRM}"+acc.getBalance());
        } else {
            mess.send("{ERR}Not owner of land ID# {PRM}"+id);
        }
    }

    public void buyLand(CommandSender sender) {
        Messaging mess = new Messaging(sender);
        if ( sender instanceof Player ) {
            String playerName = ((Player)sender).getName();
            if ( iConomyLand.tmpCuboidMap.containsKey(playerName) ) {
                Cuboid newCuboid = iConomyLand.tmpCuboidMap.get(playerName);
                if ( newCuboid.isValid() ) {
                    Account acc = iConomy.getBank().getAccount(playerName);
                    double price = Double.valueOf(iConomyLand.df.format(iConomyLand.landMgr.getPrice(newCuboid)));
                    if ( acc.getBalance() > price ) {
                        if ( iConomyLand.landMgr.addLand(newCuboid, playerName, "", "") ) {
                            acc.subtract(price);
                            iConomyLand.cmdMap.remove(playerName);
                            mess.send("{}Bought selected land for {PRM}"+iConomyLand.df.format(price));
                            mess.send("{}Bank Balance: {PRM}"+iConomyLand.df.format(acc.getBalance()));
                        } else {
                            mess.send("{ERR}Error buying land");
                        } 
                    } else {
                        mess.send("{ERR}Not enough in account. Bank: "+iConomyLand.df.format(acc.getBalance())+
                                  " Price: "+iConomyLand.df.format(price)); 
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
    
    public void sellLand(Player sender, Integer id) {
        Messaging mess = new Messaging(sender);
        Land land = iConomyLand.landMgr.getLandById(id);
        
        if ( land.owner.equalsIgnoreCase(sender.getName()) ) {
        
            Account acc = iConomy.getBank().getAccount(sender.getName());
            double price = Double.valueOf(iConomyLand.df.format(land.getSalePrice()));
            
            acc.add(price);
            iConomyLand.landMgr.removeLandById(id);
            
            mess.send("{}Sold land ID# {PRM}"+id+"{} for {PRM}"+price);
            mess.send("{}Bank Balance: {PRM}"+acc.getBalance());
        } else {
            mess.send("{ERR}Not owner of land ID# {PRM}"+id);
        }
    }
    
    
    public void showLandInfo(Player sender, String... args) {
        Messaging mess = new Messaging(sender);
        
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
            Integer landid = iConomyLand.landMgr.getLandId(loc);
            if ( landid > 0 ) {
                iConomyLand.landMgr.showSelectLandInfo((CommandSender)sender, landid);
            } else {
                mess.send("{ERR}No current selection, not on owned land");
            }
        }
            
    }
    
    public void showLandInfo(CommandSender sender, String... args) {
        Messaging mess = new Messaging(sender);

        if ( args.length == 0 ) {
            showHelp(sender,"info");
        } else {
            Integer id = 0;          
            if ( args[0].equalsIgnoreCase("here") ) {
                id = iConomyLand.landMgr.getLandId(((Player)sender).getLocation());
            } else {
                try {
                    id = Integer.parseInt(args[0]);
                } catch (NumberFormatException e ) {
                    id = 0;
                }
            }
            
            if ( id > 0 ) {
                if ( iConomyLand.landMgr.landIdExists(id)) {
                    iConomyLand.landMgr.showSelectLandInfo((CommandSender)sender, id);
                } else {
                    mess.send("{ERR}Land ID# "+id+" does not exist");
                }
            } else {
                if ( sender instanceof Player ) 
                    showLandInfo((Player)sender, args);
                else
                    showHelp(sender,"info");
            }
        }
    }
    
    public void showHelp(CommandSender sender, String topic) {
        Messaging mess = new Messaging(sender);
        mess.send("{}"+Misc.headerify("{CMD}" + iConomyLand.name + " {BKT}({CMD}" + iConomyLand.codename + "{BKT}){}"));
    	if ( topic == null || topic.isEmpty() ) {
    	    
    	    mess.send(" {CMD}/icl {}- main command");
    	    mess.send(" {CMD}/icl {PRM}help {BKT}[{PRM}topic{BKT}] {}- help topics");
    	    String topics = "";
            if ( iConomyLand.hasPermission(sender, "list") ) topics += "list";
            if ( iConomyLand.hasPermission(sender, "select") ) topics += " select";
            if ( iConomyLand.hasPermission(sender, "info") ) topics += " info";
            if ( iConomyLand.hasPermission(sender, "edit") ) topics += " edit";
            if ( iConomyLand.hasPermission(sender, "buy") ) topics += " buy";
            if ( iConomyLand.hasPermission(sender, "sell") ) topics += " buy";
            if ( iConomyLand.hasPermission(sender, "modify") ) topics += " modify";
    	    
    	    mess.send(" {} help topics: {CMD}" + topics);
    	    
    	} else if ( topic.equalsIgnoreCase("list") ) {
            if ( iConomyLand.hasPermission(sender, "list") ) { 
                mess.send(" {CMD}/icl {PRM}list {}- lists owned land");
            }
            
        } else if ( topic.equalsIgnoreCase("select") ) {
            if ( iConomyLand.hasPermission(sender, "select") ) { 
                mess.send(" {CMD}/icl {PRM}select {}- start cuboid selection process");
                mess.send("    {}After typing this command, right click ( use something");
                mess.send("    {}unplaceable ) on the first corner, then right click");
                mess.send("    {}on the second corner.");
            }
        } else if ( topic.equalsIgnoreCase("info") ) {
            if ( iConomyLand.hasPermission(sender, "info") ) { 
                mess.send(" {CMD}/icl {PRM}info {BKT}[{PRM}here{BKT}|{PRM}LANDID{BKT] {}- gets land info");
                mess.send("    {}Optional arguments 'here' or <LANDID>");
                mess.send("    {}Will give info on the selected land, or current location, or specific land ID#");
            }
            
        } else if ( topic.equalsIgnoreCase("buy") ) {
            if ( iConomyLand.hasPermission(sender, "buy") ) { 
                mess.send(" {CMD}/icl {PRM}buy {BKT}[{PRM}land{BKT}|{PRM}addon{BKT}] [{PRM}ADDON{BKT}] [{PRM}LANDID{BKT}] {}- purchase land or addons");
                mess.send("    {}this command can be used to purchase land: {CMD}/icl buy land");
                mess.send("    {}it can also be used to buy addons for a specific land ID# with:");
                mess.send("    {}{CMD}/icl buy addon <ADDON> <LANDID>");
            }
            
        } else if ( topic.equalsIgnoreCase("sell") ) {
            if ( iConomyLand.hasPermission(sender, "sell") ) { 
                mess.send(" {CMD}/icl {PRM}sell {BKT}[{PRM}land{BKT}|{PRM}addon{BKT}] [{PRM}ADDON{BKT}] [{PRM}LANDID{BKT}] {}- purchase land or addons");
                mess.send("    {}this command can be used to sell land: {CMD}/icl sell land");
                mess.send("    {}it can also be used to sell addons for a specific land ID# with:");
                mess.send("    {}{CMD}/icl sell addon <ADDON> <LANDID>");
            }

        } else if ( topic.equalsIgnoreCase("edit") ) {
            if ( iConomyLand.hasPermission(sender, "edit") ) {
                mess.send(" {CMD}/icl {PRM}edit {BKT}<{PRM}LANDID{BKT}> <{PRM}perms{BKT}|{PRM}name{BKT}> <{PRM}tags{BKT}>");
                mess.send("    {}modifies config for land ( permissions, names )");
                mess.send("    {}change location name example: {CMD}/icl edit 4 name This Land");
                mess.send("    {}Tags for perms: {BKT}<{PRM}playerName{BKT}>{PRM}:{BKT}<{PRM}t{BKT}|{PRM}f{BKT}|{PRM}-{BKT}>");
                mess.send("    {}{BKT}<{PRM}playerName{BKT}> {}- player to be affected ( or '{PRM}default{}' )");
                mess.send("    {}{BKT}<{PRM}t{BKT}|{PRM}f{BKT}|{PRM}-{BKT}> {}- {PRM}t{}/{PRM}f{} for true/false (build/destroy)");
                mess.send("    {}{PRM}- {}removes perm for playerName");
                mess.send("    {}perm example: {CMD}/icl edit 4 perms default:f kigam:t jesus:t");
            }

        } else if ( topic.equalsIgnoreCase("modify") ) {
            if ( iConomyLand.hasPermission(sender, "modify") ) { 
                mess.send(" {CMD}/icl {PRM}modify {BKT}<{PRM}LANDID{BKT}> <{PRM}perms{BKT}|{PRM}addons{BKT}> <{PRM}tags{BKT}> {}- modify land settings");
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

package me.slaps.iCoLand;

import java.io.File;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.iConomy.iConomy;
import com.iConomy.system.Account;

public class iCoLandCommandListener implements CommandExecutor {
    
    Random rn = new Random();
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Messaging mess = new Messaging(sender);

        if ( Config.debugMode1 ) {
            String debug = "iCoLand.onCommand(): " + ((sender instanceof Player) ? "Player " + ((Player) sender).getName() : "Console") + " Command " + cmd.getName() + " args: ";
            for (int i = 0; i < args.length; i++) 
                debug += args[i] + " ";
            iCoLand.info(debug);
        }

        // is our command?
        if ( Misc.isAny(cmd.getName(), "icl", "iCoLand", "iCoLand:icl", "iCoLand:iCoLand") ) {
            if (Config.debugMode) iCoLand.info("Is an /icl or /iCoLand command");

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
                if ( iCoLand.hasPermission(sender, "land.list") ) { 
                    if ( args.length > 1 ) {
                        Integer page;
                        try { 
                            page = Integer.parseInt(args[1]);
                            if ( page > 1 ) 
                                showList(sender, page-1, false);
                            else
                                mess.send("{ERR}Bad page #");
                        } catch(NumberFormatException ex) {
                            mess.send("{ERR}Error parsing page #!");                        
                        }
                    } else {
                        showList(sender, 0, false);
                    }
                } else {
                    mess.send("{ERR}No access for list");
                }
                return true;
                
            } else if (args[0].equalsIgnoreCase("adminlist") ) {
                if ( args.length > 1 ) {
                    Integer page;
                    try { 
                        page = Integer.parseInt(args[1]);
                        if ( page > 1 ) 
                            showList(sender, page-1, true);
                        else
                            mess.send("{ERR}Bad page #");
                    } catch(NumberFormatException ex) {
                        mess.send("{ERR}Error parsing page #!");                        
                    }
                } else {
                    showList(sender, 0, true);
                }
                return true;
                
            // /icl edit <LANDID> <name|perms> <tags>
            } else if (args[0].equalsIgnoreCase("edit") ) {
                if ( !(sender instanceof Player) ) {
                    mess.send("{ERR}Console can not edit with this command");
                } else if ( iCoLand.hasPermission(sender, "land.edit") ) {
                    Player player = (Player)sender;
                    if ( args.length > 3 ) {
                        Integer id;
                        try { 
                            id = Integer.parseInt(args[1]); 
                        } catch (NumberFormatException ex) { 
                            mess.send("{ERR}Error reading <LANDID>");
                            return true;
                        }
                        if ( iCoLand.landMgr.landIdExists(id) ) {
                            if ( iCoLand.landMgr.isOwner(player.getName(), id) ) {                            
                                if ( Misc.isAny(args[2], "name", "perms", "nospawn") ) {
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
                
            // /icl modify <id> <perms|addons|name> <tags>
            } else if (args[0].equalsIgnoreCase("modify") ) {
                if ( iCoLand.hasPermission(sender, "admin.modify") ) {
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
                        if ( !iCoLand.landMgr.landIdExists(id) ) {
                            mess.send("{ERR}Land ID# {PRM}" + id + " {ERR}doesn't exist");
                        } else {
                            if ( Misc.isAny(args[2], "perms", "addons", "owner", "name") ) {
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
                if ( iCoLand.hasPermission(sender, "basic.select") ) {
                    if ( !(sender instanceof Player) ) {
                        mess.send("Console can't select");
                    } else if ( args.length == 1 ) {
                        selectArea((Player)sender);
                    } else if ( args.length == 2 ) {
                        Player player = (Player)sender;
                        if ( args[1].equalsIgnoreCase("cancel") ) {
                            mess.send("{}Cancelling current selection.");
                            iCoLand.cmdMap.remove(player.getName());
                            iCoLand.tmpCuboidMap.remove(((Player)sender).getName());
                        } else if ( args[1].equalsIgnoreCase("fullheight") ) {
                            if ( iCoLand.tmpCuboidMap.containsKey(player.getName()) ) {
                                mess.send("{}Changing selection height to full height.");
                                iCoLand.tmpCuboidMap.get(player.getName()).setFullHeight();
                                int id = iCoLand.landMgr.intersectsExistingLand(iCoLand.tmpCuboidMap.get(player.getName()));
                                if ( id > 0 ) {
                                    mess.send("{ERR}Intersects existing land!");
                                    iCoLand.tmpCuboidMap.put(player.getName(), iCoLand.landMgr.getLandById(id).location);
                                } else {
                                    mess.send("{}Selecting full height");
                                }
                            } else {
                                mess.send("{ERR}No currently selected land.");
                            }
                        } else {
                            int i = -1;
                            try {
                                i = Integer.parseInt(args[1]);
                            } catch (NumberFormatException ex) {
                            }
                            if ( i > 0 ) {
                                if ( selectLand((Player)sender, i) ) {
                                    mess.send("{}Selected land ID#{PRM}"+i);
                                } else {
                                    mess.send("{ERR}No land ID#{PRM}"+i);
                                }
                            } else {
                                mess.send("Error parsing argument");
                            }
                        }
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
                if ( iCoLand.hasPermission(sender, "basic.info") ) {
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
                } else if ( iCoLand.hasPermission(sender, "land.buy") ) {
                    if ( args.length == 1 ) {
                        mess.send("{ERR}Not enough arguments");
                        showHelp(sender,"buy");
                    } else {
                        if ( args.length == 2 && args[1].equalsIgnoreCase("land") ) {
                            buyLand(sender);
                        } else if ( args.length > 2 && args[1].equalsIgnoreCase("addon") ) {
                            if ( args.length == 4 ) {
                                if ( Config.isAddon(args[2]) ) {
                                    try { 
                                        Integer id = Integer.parseInt(args[3]);
                                            if ( iCoLand.landMgr.landIdExists(id) ) {
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
                } else if ( iCoLand.hasPermission(sender, "land.sell") ){
                    if ( args.length == 3 && args[1].equalsIgnoreCase("land") ) {
                        Integer id = 0;
                        try {
                            id = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ex) {
                            mess.send("{ERR}Error parsing ID#");
                        }
                        
                        if ( iCoLand.landMgr.landIdExists(id) ) {
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
                        
                        if ( iCoLand.landMgr.landIdExists(id) ) {
                            if ( Config.isAddon(args[2]) ) {
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
                
            } else if ( args[0].equalsIgnoreCase("importdb") ) {
                if ( iCoLand.hasPermission(sender, "admin.importdb") ) {
                    iCoLand.landMgr.importDB(new File(iCoLand.pluginDirectory + File.separator + Config.importFile));
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;
                
            } else if ( args[0].equalsIgnoreCase("exportdb") ) {
                if ( iCoLand.hasPermission(sender, "admin.exportdb") ) {
                    iCoLand.landMgr.exportDB(new File(iCoLand.pluginDirectory + File.separator + Config.exportFile));
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;

            } else if ( args[0].equalsIgnoreCase("fakedata") ) {
                if ( iCoLand.hasPermission(sender, "admin.fakedata") ) {
                    if ( args.length > 1 ) {
                        Integer numLands = 0;
                        try { 
                            numLands = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                        long start = System.currentTimeMillis();
                        for(int i=0;i<numLands;i++) {
                            Location loc1 = new Location(iCoLand.server.getWorlds().get(0), rand(-4096,4096), rand(5,100), rand(-4096,4096));
                            Location loc2 = new Location(iCoLand.server.getWorlds().get(0), loc1.getBlockX()+rand(0,100), loc1.getBlockY()+rand(0,28), loc1.getBlockZ()+rand(0,100));
                            iCoLand.landMgr.addLand(new Cuboid(loc1,loc2), ((sender instanceof Player)?((Player)sender).getName():"kigam"), "", "");
                        }
                        if ( Config.debugMode ) iCoLand.info("Inserting "+numLands+" random lands took: "+(System.currentTimeMillis()-start)+" ms");
                        
                    } else {
                        mess.send("{ERR}Not enough arguments");
                    }
                } else {
                    mess.send("{ERR}No access for that...");
                }
                return true;
                
            } else if ( args[0].equalsIgnoreCase("fixperms") ) {
                if ( iCoLand.hasPermission(sender, "admin.fixperms") ) {
                    ArrayList<Land> lands = iCoLand.landMgr.getAllLands();
                    for(Land land: lands) {
                        iCoLand.landMgr.updatePerms(land.getID(), Land.writePermTags(land.canBuildDestroy));
                    }
                    mess.send("{}Done...");
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
    
    public int rand(int lo, int hi) {
        int n = hi - lo + 1;
        int i = rn.nextInt() % n;
        if ( i < 0 ) 
            i = -i;
        return lo+i;
    }
    
    public boolean selectLand(Player player, int id) {
        String playerName = player.getName();
        Land land = iCoLand.landMgr.getLandById(id);
        if ( land != null ) {
            iCoLand.tmpCuboidMap.put(playerName, land.location);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean selectArea(Player player) {
        String playerName = player.getName();
        Messaging mess = new Messaging((CommandSender)player);
        if ( iCoLand.cmdMap.containsKey(playerName) && iCoLand.cmdMap.get(playerName).equals("select") ) {
            mess.send("{ERR}Cancelling selection command.");
            iCoLand.cmdMap.remove(playerName);
        }
        
        if ( iCoLand.tmpCuboidMap.containsKey(playerName) ) {
            mess.send("{ERR}Unselecting current cuboid");
            iCoLand.tmpCuboidMap.remove(playerName);
        }
            
        mess.send("{}Left Click 1st Corner");
        iCoLand.cmdMap.put(playerName,"select");
        return true;
    }
    
    public void editLand(Player player, Integer id, String category, String args) {
        Messaging mess = new Messaging(player);
        if ( category.equals("perms") ) {
            if (iCoLand.landMgr.modifyPermTags(id, args)) 
                mess.send("{}Permissions modified");
            else
                mess.send("{ERR}Problem modify permissions");
        } else if ( category.equalsIgnoreCase("name") ) {
            if ( iCoLand.landMgr.updateName(id, args.substring(0, (args.length()>35)?35:args.length())) )
                mess.send("{}Location name changed");
            else
                mess.send("{ERR}Error changing location name");
        } else if ( category.equalsIgnoreCase("nospawn") ) {
            if ( iCoLand.landMgr.modifyNoSpawnTags(id, args) ) 
                mess.send("{}NoSpawn list modified");
            else
                mess.send("{ERR}Problem modifying NoSpawn list");
        }
    }
    
    public void adminEditLand(CommandSender sender, Integer id, String category, String tags) {
        Messaging mess = new Messaging(sender);
        if ( category.equals("perms") ) {
            if (iCoLand.landMgr.modifyPermTags(id, tags)) 
                mess.send("{}Permissions modified");
            else
                mess.send("{ERR}Problem modify permissions");
        } else if ( category.equalsIgnoreCase("owner") ) {
            if ( iCoLand.landMgr.updateOwner(id, tags) )
                mess.send("{}Owner changed");
            else
                mess.send("{ERR}Problem modifying owner");
        } else if ( category.equalsIgnoreCase("addons") ) {
            if ( iCoLand.landMgr.toggleAddons(id, tags) )
                mess.send("{}Addons modified");
            else
                mess.send("{ERR}Error modifying addons");
        } else if ( category.equalsIgnoreCase("name") ) {
            if ( iCoLand.landMgr.updateName(id, tags.substring(0, (tags.length()>35)?35:tags.length())) )
                mess.send("{}Location name changed");
            else
                mess.send("{ERR}Error changing location name");
        } else if ( category.equalsIgnoreCase("nospawn") ) {
            if ( iCoLand.landMgr.modifyNoSpawnTags(id, tags) ) 
                mess.send("{}NoSpawn list modified");
            else
                mess.send("{ERR}Problem modifying NoSpawn list");            
        } else {
            mess.send("{ERR}Bad category");
        }
        
    }
    
    public void showList(CommandSender sender, Integer page, boolean showAll) {
        Integer pageSize = 7;
        ArrayList<Land> list;

        int numLands = 0;
        if( !showAll && (sender instanceof Player) )
            numLands = iCoLand.landMgr.countLandsOwnedBy(((Player)sender).getName());
        else
            numLands = iCoLand.landMgr.countLandsOwnedBy(null);
                
        Messaging mess = new Messaging(sender);
        if ( numLands == 0 ) {
            mess.send("{ERR}You do not own any land");
        } else {
            if ( page*pageSize > numLands ) {
                mess.send("{ERR}No lands on this page");
            } else {
                String playerName = (!showAll && (sender instanceof Player))?((Player)sender).getName():null;
                list = iCoLand.landMgr.getLandsOwnedBy(playerName, pageSize, page*pageSize);
                int pages = numLands / pageSize + (( numLands % pageSize > 0)?1:0);
                mess.send("{}"+Misc.headerify("{CMD}Your Lands {BKT}({CMD}Page " + (page+1) + "{BKT}/{CMD}"+pages+"{BKT}){}"));                
                int i;
                for(i=0;i<pageSize && i<list.size();i++) {
                    Land land = list.get(i);
                    mess.send("{PRM}ID#{}"+land.getID()+((land.active?"":"({ERR}I{})"))+
                            " "+
                            ((land.locationName.isEmpty())?"":land.locationName+" ") +
                            "{PRM}V:{}"+land.location.volume()+" "+
                            "{PRM}[{}"+land.location.toDimString()+
                            "{PRM}] C{}"+land.location.toCenterCoords()
                            );
                }
            }
            
        }
        
    }
 
    public void buyAddon(Player sender, String addon, Integer id) {
        Messaging mess = new Messaging(sender);
        Land land = iCoLand.landMgr.getLandById(id);

        if ( land.owner.equalsIgnoreCase(sender.getName()) ) {
            Account acc = iConomy.getAccount(sender.getName());
            Account bank = iConomy.getAccount(Config.bankName);
            double price = Double.valueOf(iCoLand.df.format(iCoLand.landMgr.getLandById(id).getAddonPrice(addon)));
            
            if ( iCoLand.hasPermission(sender, "admin.nocost") ) {
                if ( iCoLand.landMgr.addAddon(id, addon) )
                    mess.send("{}Bought addon {PRM}"+addon+"{} for {PRM}0 {BKT}({PRM}"+iCoLand.df.format(price)+"{BKT})");
                else
                    mess.send("{ERR}Error buying addon");
            } else if ( acc.getHoldings().balance() > price ) {
                if ( iCoLand.landMgr.addAddon(id, addon) ) {
                    acc.getHoldings().subtract(price);
                    bank.getHoldings().add(price);
                    
                    mess.send("{}Bought addon {PRM}"+addon+"{} for {PRM}"+iCoLand.df.format(price));
                    mess.send("{}Bank Balance: {PRM}"+iCoLand.df.format(acc.getHoldings().balance()));
                } else {
                    mess.send("{ERR}Error buying addon");
                }
            } else {
                mess.send("{ERR}Not enough in account. Bank: "+iCoLand.df.format(acc.getHoldings().balance())+
                        " Price: "+iCoLand.df.format(price)); 
            }
        } else {
            mess.send("{ERR}Not owner of land ID# {PRM}"+id);
        }
    }
    
    public void sellAddon(Player sender, String addon, Integer id) {
        Messaging mess = new Messaging(sender);
        Land land = iCoLand.landMgr.getLandById(id);
        
        if ( land.owner.equalsIgnoreCase(sender.getName()) ) {
            Account acc = iConomy.getAccount(sender.getName());
            Account bank = iConomy.getAccount(Config.bankName);
            double price = Double.valueOf(iCoLand.df.format(land.getAddonPrice(addon)));
            double sellPrice = price*Config.sellTax;
            
            if ( iCoLand.hasPermission(sender, "admin.nocost") ) {
                if ( iCoLand.landMgr.removeAddon(id, addon) )
                    mess.send("{}Sold addon {PRM}"+addon+" on land ID# {PRM}"+id+"{} for {PRM}0 {BKT}({PRM}"+price+"{BKT})");
                else
                    mess.send("{ERR}Error selling addon");
            } else {
                if ( iCoLand.landMgr.removeAddon(id, addon) ) {
                    acc.getHoldings().add(sellPrice);
                    bank.getHoldings().subtract(sellPrice);
                    mess.send("{}Sold addon {PRM}"+addon+" on land ID# {PRM}"+id+"{} for {PRM}"+price);
                    mess.send("{}Bank Balance: {PRM}"+acc.getHoldings().balance());
                } else {
                    mess.send("{ERR}Error selling addon");
                }

            }
        
        } else {
            mess.send("{ERR}Not owner of land ID# {PRM}"+id);
        }
    }

    public void purchaseLand(Player player, Cuboid newCuboid) {
        Messaging mess = new Messaging(player);
        String playerName = player.getName();
        Account acc = iConomy.getAccount(playerName);        
        Account bank = iConomy.getAccount(Config.bankName);
        double price = Double.valueOf(iCoLand.df.format(iCoLand.landMgr.getPrice(newCuboid)));
        
        if ( (acc.getHoldings().balance() > price) || iCoLand.hasPermission(player, "admin.nocost") ) {
            if ( iCoLand.landMgr.addLand(newCuboid, playerName, playerName+":t", "") ) {
                if ( iCoLand.hasPermission(player, "admin.nocost") ) {
                    mess.send("{}Bought selected land for {PRM}0 {BKT}({PRM}"+iCoLand.df.format(price)+"{BKT})");
                } else {
                    acc.getHoldings().subtract(price);
                    bank.getHoldings().add(price);
                    mess.send("{}Bought selected land for {PRM}"+iCoLand.df.format(price));
                    mess.send("{}Bank Balance: {PRM}"+iCoLand.df.format(acc.getHoldings().balance()));
                }
                iCoLand.cmdMap.remove(playerName);
            } else {
                mess.send("{ERR}Error buying land");
            }
        } else {
            mess.send("{ERR}Not enough in account. Bank: "+iCoLand.df.format(acc.getHoldings().balance())+
                      " Price: "+iCoLand.df.format(price)); 
        }
    }

    public void reactivateLand( Player player, int id ) {
        Messaging mess = new Messaging(player);
        String playerName = player.getName();
        Account acc = iConomy.getAccount(playerName);        
        Account bank = iConomy.getAccount(Config.bankName);
        Land land = iCoLand.landMgr.getLandById(id);
        double tax = Double.valueOf(iCoLand.df.format(land.location.volume()*Config.pricePerBlock.get("raw")*Config.taxRate));
        
        
        if ( iCoLand.hasPermission(player, "admin.nocost") || iCoLand.hasPermission(player, "admin.notax") ) {
            iCoLand.landMgr.updateActive(id, true);
            iCoLand.landMgr.updateTaxTime(id, new Timestamp(System.currentTimeMillis()));
            mess.send("Land bought back for {PRM}0 {}("+iCoLand.df.format(tax)+")");
        } else if ( acc.getHoldings().hasEnough(tax) ) {
            acc.getHoldings().subtract(tax);
            bank.getHoldings().add(tax);
            iCoLand.landMgr.updateActive(id, true);
            iCoLand.landMgr.updateTaxTime(id, new Timestamp(System.currentTimeMillis()));
            mess.send("Land bought back for {PRM}"+iCoLand.df.format(tax));
        } else {
            mess.send("{ERR}Not enough money to pay past due taxes of {PRM}"+iCoLand.df.format(tax));
        }
        
    }
    
    public void buyLand(CommandSender sender) {
        Messaging mess = new Messaging(sender);
        if ( sender instanceof Player ) {
            String playerName = ((Player)sender).getName();
            if ( iCoLand.tmpCuboidMap.containsKey(playerName) ) {
                Cuboid newCuboid = iCoLand.tmpCuboidMap.get(playerName);
                int id = iCoLand.landMgr.intersectsExistingLand(newCuboid);
                Land land;
                if ( id > 0 ) {
                    land =  iCoLand.landMgr.getLandById(id);
                    if ( land.location.equals(newCuboid) ) {
                        if ( land.active ) {
                            mess.send("{ERR}Can't buy active land");
                        } else {
                            reactivateLand((Player)sender, id);
                        }
                    } else {
                        mess.send("{ERR}selection doesn't match zone");
                    }
                } else if ( newCuboid.isValid() ) {
                    if ( iCoLand.hasPermission(sender, "admin.nolimits") ) {
                        purchaseLand((Player)sender, newCuboid);
                    } else {
                        if ( newCuboid.volume() <= Config.maxLandVolume ) {
                            if ( newCuboid.volume() >= Config.minLandVolume ) {                    
                                if ( iCoLand.landMgr.canClaimMoreVolume(playerName, newCuboid.volume() ) ) {
                                    if ( iCoLand.landMgr.canClaimMoreLands(playerName) ) {
                                        purchaseLand((Player)sender, newCuboid);
                                    } else {
                                        mess.send("{ERR}Can not claim over "+Config.maxLandsClaimable+" lands!");
                                    }
                                } else {
                                    mess.send("{ERR}Can not claim over "+Config.maxBlocksClaimable+" blocks!");
                                }
                            } else {
                                mess.send("{ERR}Volume must be at least "+Config.minLandVolume+" blocks!");
                            }
                        } else {
                            mess.send("{ERR}Too large, max volume is "+Config.maxLandVolume+" blocks!");
                        }
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
        Land land = iCoLand.landMgr.getLandById(id);
        
        if ( land.owner.equalsIgnoreCase(sender.getName()) ) {
            if ( land.active ) {
                Account acc = iConomy.getAccount(sender.getName());
                Account bank = iConomy.getAccount(Config.bankName);
                double price = Double.valueOf(iCoLand.df.format(land.getTotalPrice()));
                double sellPrice = Double.valueOf(iCoLand.df.format(price*Config.sellTax));
    
                if ( iCoLand.hasPermission(sender, "admin.nocost") ) {
                    iCoLand.landMgr.removeLandById(id);
                    mess.send("{}Sold land ID# {PRM}"+id+"{} for {PRM}0 {BKT}({PRM}"+sellPrice+"{BKT})");
                } else {
                    acc.getHoldings().add(sellPrice);
                    bank.getHoldings().subtract(sellPrice);
                    
                    iCoLand.landMgr.removeLandById(id);
                    mess.send("{}Sold land ID# {PRM}"+id+"{} for {PRM}"+sellPrice);
                    mess.send("{}Bank Balance: {PRM}"+acc.getHoldings().balance());
                }
            } else {
                iCoLand.landMgr.removeLandById(id);
                mess.send("{}Got rid of inactive land ID# {PRM}"+id);
            }
            
        } else {
            mess.send("{ERR}Not owner of land ID# {PRM}"+id);
        }
    }
    
    public void showHelp(CommandSender sender, String topic) {
        Messaging mess = new Messaging(sender);
        mess.send("{}"+Misc.headerify("{CMD}" + iCoLand.name + " {BKT}({CMD}" + iCoLand.codename + "{BKT}){}"));
    	if ( topic == null || topic.isEmpty() ) {
    	    
    	    mess.send(" {CMD}/icl {}- main command");
    	    mess.send(" {CMD}/icl {PRM}help {BKT}[{PRM}topic{BKT}] {}- help topics");
    	    String topics = "";
            if ( iCoLand.hasPermission(sender, "basic.select") ) topics += "select";
            if ( iCoLand.hasPermission(sender, "basic.info") ) topics += " info";
            if ( iCoLand.hasPermission(sender, "land.list") ) topics += " list";
            if ( iCoLand.hasPermission(sender, "land.edit") ) topics += " edit";
            if ( iCoLand.hasPermission(sender, "land.buy") ) topics += " buy";
            if ( iCoLand.hasPermission(sender, "land.sell") ) topics += " sell";
            if ( iCoLand.hasPermission(sender, "admin.modify") ) topics += " modify";
    	    
    	    mess.send(" {} help topics: {CMD}" + topics);
    	    
    	} else if ( topic.equalsIgnoreCase("list") ) {
            if ( iCoLand.hasPermission(sender, "land.list") ) { 
                mess.send(" {CMD}/icl {PRM}list {BKT}[{PRM}PAGE{BKT}] {}- lists owned land");
            }
            
        } else if ( topic.equalsIgnoreCase("select") ) {
            if ( iCoLand.hasPermission(sender, "basic.select") ) { 
                mess.send(" {CMD}/icl {PRM}select {}- start cuboid selection process");
                mess.send("    {}Left click a block to set the 1st corner, then left");
                mess.send("    {}click a 2nd block to set the 2nd corner.  This selects");
                mess.send("    {}the {BKT}CUBE{} set by the 2 corner blocks.");
                mess.send(" {CMD}/icl {PRM}select {BKT}[{PRM}cancel{BKT}|{PRM}fullheight{BKT}] {}");
                mess.send("    {}cancel - cancels current selection");
                mess.send("    {}fullheight - changes current selection to full height");
            }
        } else if ( topic.equalsIgnoreCase("info") ) {
            if ( iCoLand.hasPermission(sender, "basic.info") ) { 
                mess.send(" {CMD}/icl {PRM}info {BKT}[{PRM}here{BKT}|{PRM}LANDID{BKT}]");
                mess.send("    {}Gives land information.");
                mess.send("    {PRM}here {}- info on land where you are standing");
                mess.send("    {PRM}<LANDID> {}- info on specific land ID");
            }
            
        } else if ( topic.equalsIgnoreCase("buy") ) {
            if ( iCoLand.hasPermission(sender, "land.buy") ) { 
                mess.send(" {CMD}/icl {PRM}buy {BKT}[{PRM}land{BKT}|{PRM}addon{BKT}] [{PRM}ADDON{BKT}] [{PRM}LANDID{BKT}] {}");
                mess.send("    {}Purchase land or addons for lands.");
                mess.send("    {}this command can be used to purchase land: {CMD}/icl {PRM}buy land");
                mess.send("    {}it can also be used to buy addons for a land ID# with:");
                mess.send("    {}{CMD}/icl {PRM}buy addon <ADDON> <LANDID>");
                String out = "    {}Addons avail: {PRM}";
                Set<String> addons = Config.addonsEnabled.keySet();
                for(String addon : addons) {
                    if ( Config.addonsEnabled.get(addon) )
                        out += addon + "{}, {PRM}"; 
                }
                if ( out.substring(out.length()-9, out.length()).equals("{}, {PRM}") ) out = out.substring(0,out.length()-9);
                mess.send(out);
            }
            
        } else if ( topic.equalsIgnoreCase("sell") ) {
            if ( iCoLand.hasPermission(sender, "land.sell") ) { 
                mess.send(" {CMD}/icl {PRM}sell {BKT}[{PRM}land{BKT}|{PRM}addon{BKT}] [{PRM}ADDON{BKT}] [{PRM}LANDID{BKT}]");
                mess.send("    {}this command can be used to sell land: {CMD}/icl {PRM}sell land");
                mess.send("    {}it can also be used to sell addons for a land ID# with:");
                mess.send("    {}{CMD}/icl {PRM}sell addon <ADDON> <LANDID>");
            }

        } else if ( topic.equalsIgnoreCase("edit") ) {
            if ( iCoLand.hasPermission(sender, "land.edit") ) {
                mess.send(" {CMD}/icl {PRM}edit {BKT}<{PRM}LANDID{BKT}> <{PRM}perms{BKT}|{PRM}name{BKT}|{PRM}nospawn{BKT}> <{PRM}tags{BKT}>");
                mess.send("    {}modifies config for land ( permissions, names )");
                mess.send("    {}change location name example: {CMD}/icl edit 4 name This Land");
                mess.send("    {}Tags for perms: {BKT}<{PRM}playerName{BKT}>{PRM}:{BKT}<{PRM}t{BKT}|{PRM}f{BKT}|{PRM}-{BKT}>");
                mess.send("    {}{BKT}<{PRM}playerName{BKT}> {}- player to be affected");
                mess.send("    {}{BKT}<{PRM}t{BKT}|{PRM}f{BKT}|{PRM}-{BKT}> {}- {PRM}t{}/{PRM}f{} for true/false (build/destroy)");
                mess.send("    {}{PRM}- {}removes perm for playerName");
                mess.send("    {}perm example: {CMD}/icl edit 4 perms default:f kigam:t jesus:t");
            }

        } else if ( topic.equalsIgnoreCase("modify") ) {
            if ( iCoLand.hasPermission(sender, "admin.modify") ) { 
                mess.send(" {CMD}/icl {PRM}modify {BKT}<{PRM}LANDID{BKT}> <{PRM}perms{BKT}|{PRM}addons{BKT}|{PRM}owner{BKT}|{PRM}name{BKT}|{PRM}nospawn{BKT}> <{PRM}tags{BKT}> {}- modify land settings");
            }
            
        }

    }

    public void showSelectLandInfo(CommandSender sender, Cuboid select) {
        Messaging mess = new Messaging(sender);
        Integer id = iCoLand.landMgr.intersectsExistingLand(select);
        
        if ( id > 0 && iCoLand.landMgr.getLandById(id).location.equals(select) ) {
            showExistingLandInfo(sender, iCoLand.landMgr.getLandById(id));
        } else if ( id > 0 ) {
            mess.send("{ERR}Intersects existing land ID# "+id);
            mess.send("{ERR}Selecting/showing land ID# "+id+" instead");
            iCoLand.tmpCuboidMap.put(((Player)sender).getName(), iCoLand.landMgr.getLandById(id).location );
            showExistingLandInfo(sender, iCoLand.landMgr.getLandById(id));
        } else {
            mess.send("{}"+Misc.headerify("{PRM}Unclaimed Land{}"));
            mess.send("Dimensoins: " + select.toDimString() );
            mess.send("Volume: " + select.volume() );
            mess.send("Price: " + iCoLand.df.format(iCoLand.landMgr.getPrice(select)));
        }
        
    }
    
    public void showSelectLandInfo(CommandSender sender, Integer id) {
        showExistingLandInfo(sender, iCoLand.landMgr.getLandById(id));
    }
    
    public String formatTimeLeft(long due) {
        String ret = "";
        long now = System.currentTimeMillis();
        
        DecimalFormat df = new DecimalFormat("#");
        
        long secsleft = (long) (Math.abs(due-now)/1000.0);
        long minsleft = (long) (Math.abs(due-now)/1000.0/60.0);
        if ( due - now < 0 ) {
            ret += "-";
        }
        
        if ( minsleft > 1440 )  {
            long daysleft = Long.parseLong(df.format(secsleft/60.0/60.0/24.0));
            long hoursleft = (minsleft/60)%daysleft;
            ret += daysleft+" day"+((daysleft>1)?"s":"")+
            ((hoursleft>0)?
                    ", "+hoursleft+" hour"+((hoursleft>1)?"s":"")
             :"");
        } else if ( minsleft > 60 ) {
            long hoursleft = Long.parseLong(df.format(minsleft/60.0));
            minsleft = minsleft - ( hoursleft*60 );
            ret += hoursleft+" hour"+((hoursleft>1)?"s":"")+
                ((hoursleft == 1 )?", "+minsleft+" minute"+((minsleft > 1)?"s":""):"");
        } else if ( minsleft > 0 ) {
            secsleft = secsleft - ( minsleft*60 );
            ret += minsleft+" minute"+((minsleft>1)?"s":"")+
                ((minsleft < 5)?", "+secsleft+" second"+((secsleft>1)?"s":""):"");
        } else if ( secsleft >= 0 ) {
            ret += secsleft+" second"+((secsleft>1)?"s":"");
        } 
                
        return ret;
    }
    
    public void showExistingLandInfo(CommandSender sender, Land land) {
        Messaging mess = new Messaging(sender);
        mess.send("{}"+Misc.headerify("{} Land ID# {PRM}"+land.getID()+
                                      ((land.active)?"":" {ERR}INACTIVE")+
                                      "{} --"+
                                      (land.locationName.isEmpty()?"":" {PRM}"+land.locationName+" {}")
                                     ));
        mess.send("{CMD}C: {}"+land.location.toCenterCoords()+" {CMD}V: {}"+land.location.volume()+" {CMD}D: {}"+land.location.toDimString());
        mess.send("{CMD}Owner: {}"+land.owner);
        if ( !(sender instanceof Player) || land.owner.equals(((Player)sender).getName()) || iCoLand.hasPermission(sender,"admin.bypass") ) {
            if ( !land.locationName.isEmpty() )
                mess.send("{CMD}Name: {}"+land.locationName);
            
            mess.send("{CMD}Taxes Due: {PRM}"+
                    (iCoLand.hasPermission(land.location.LocMin.getWorld().getName(), land.owner, "admin.notax")?"0 {BKT}({PRM}"+(iCoLand.df.format(land.location.volume()*Config.pricePerBlock.get("raw")*Config.taxRate))+"{BKT})":
                    (iCoLand.df.format(land.location.volume()*Config.pricePerBlock.get("raw")*Config.taxRate)))+
                    " {BKT}({}"+
                    (land.active?
                    formatTimeLeft(land.dateTax.getTime()):
                    formatTimeLeft(land.dateTax.getTime()+Config.inactiveDeleteTime*60*1000))
                    +" left{BKT})"
                    );
            mess.send("{CMD}Perms: {}"+Land.writePermTags(land.canBuildDestroy)); 
            if ( land.hasAddon("nospawn") ) 
                mess.send("{CMD}NoSpawn: {}"+land.noSpawn);
            mess.send("{CMD}Addons: {}"+Land.writeAddonTags(land.addons));
            mess.send("{CMD}Addon Prices: {}"+Land.writeAddonPrices(land));
        }
    }
    
    public void showLandInfo(Player sender, String... args) {
        Messaging mess = new Messaging(sender);
        
        // use location search or selected search
        String playerName = sender.getName();
        if ( iCoLand.tmpCuboidMap.containsKey(playerName) ) {
            Cuboid select = iCoLand.tmpCuboidMap.get(playerName);
            if ( select.isValid() ) {
                showSelectLandInfo((CommandSender)sender, select);
            } else {
                mess.send("{ERR}Current selection invalid! Use {CMD}/lwc select{} to cancel");
            }
        } else {
            Location loc = sender.getLocation();
            ArrayList<Integer> landids = iCoLand.landMgr.getLandIds(loc);
            if ( landids.size() > 0 ) {
                showSelectLandInfo((CommandSender)sender, landids.get(0));
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
            if ( args[0].equalsIgnoreCase("here") ) {
                if ( sender instanceof Player ) {
                    ArrayList<Integer> ids = iCoLand.landMgr.getLandIds(((Player)sender).getLocation());
                    if ( ids.size() > 0 ) {
                        showSelectLandInfo((CommandSender)sender, ids.get(0));
                    } else {
                        mess.send("{ERR}No land claimed where you are standing.");
                    }
                } else {
                    mess.send("{ERR}Console can't use here");
                }
            } else {
                Integer id = 0;
                try {
                    id = Integer.parseInt(args[0]);
                } catch (NumberFormatException e ) {
                    id = 0;
                }
                if ( id > 0 ) {
                    if ( iCoLand.landMgr.landIdExists(id)) {
                        showSelectLandInfo((CommandSender)sender, id);
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
    }
    
    
    
}

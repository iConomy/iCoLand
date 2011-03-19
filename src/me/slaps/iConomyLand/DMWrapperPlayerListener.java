package me.slaps.DMWrapper;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
import com.gmail.haloinverse.DynamicMarket.Messaging;
import com.gmail.haloinverse.DynamicMarket.Misc;

public class DMWrapperPlayerListener extends PlayerListener {
	
	protected HashMap<String, Boolean> inShopMap = new HashMap<String, Boolean>();

	public DMWrapperPlayerListener(DMWrapper plug) {
	    plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Monitor, plug);
	    if ( DMWrapper.debugMode )
	        plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Priority.Highest, plug);
	}

    @Override
    public void onPlayerCommandPreprocess ( PlayerChatEvent event ) {
        if (DMWrapper.debugMode)
            DMWrapper.info("DMWrapperPlayerListener.onPlayerCommandPreprocess(): Player: " + 
                           event.getPlayer().getName() + " msg: " + event.getMessage() + 
                           " Canceled? " + ( event.isCancelled()? "Yes": "No" ) );
       
    }	
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
        if ( !DMWrapper.locMgr.shopLocationsEnabled ) return;
	    
	    String playerName = event.getPlayer().getName();
	    
	    if (!inShopMap.containsKey(playerName)) inShopMap.put(playerName,false);
	    
		Location to = event.getTo();
		Location blockTo = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ() );
		
		if ( DMWrapper.locMgr.inShopLoc(blockTo) ) {
		    if ( !inShopMap.get(playerName) ) {
		        event.getPlayer().sendMessage("Entered the shopping area.");
		        inShopMap.put(playerName, true);
		    }
		} else {
		    if ( inShopMap.get(playerName) ) {
                event.getPlayer().sendMessage("Leaving the shopping area.");
                inShopMap.put(playerName, false);
		    }
		}
	}	

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // debug output
        if ( DMWrapper.debugMode ) {
            String debug = "DMWrapper.onCommand(): " + ((sender instanceof Player) ? "Player " + ((Player) sender).getName() : "Console") + " Command " + cmd.getName() + " args: ";
            for (int i = 0; i < args.length; i++) 
                debug += args[i] + " ";
            DMWrapper.info(debug);
        }

        // location-based features only available to Players
        if (!(sender instanceof Player))
            return DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);

        // only intercept shop command
        if (cmd.getName().toLowerCase().equals("shop") || cmd.getName().toLowerCase().equals("dshop")) {
            if (DMWrapper.debugMode) DMWrapper.info("Is a /shop or /dshop command");

            // pass commands to DynamicMarket or intercept

            // just '/shop'
            if ( args.length == 0 ) {
                showHelp(sender, "");
                return true;
                
            // help command
            }  else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?")) {
                if ( args.length == 1 ) {
                    showHelp(sender, "");
                    return true;
                } else {
                    showHelp(sender, args[1] );
                    return true;
                }
                
            // is location based shopping enabled?
            } else if (!DMWrapper.locMgr.shopLocationsEnabled && (args.length > 0) && (!args[0].equalsIgnoreCase("location"))) {
                if (DMWrapper.debugMode) DMWrapper.info("locations disabled and not a /shop location command");
                return DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);

            // locations enabled, intercept commands
            } else {
                // not a '/shop location' command
                if (!args[0].equalsIgnoreCase("location")) {
                    if (DMWrapper.debugMode) DMWrapper.info("not a /shop location command");

                    // in a shop location? or admin?
                    if (DMWrapper.hasPermission(sender, "admin") || DMWrapper.locMgr.inShopLoc(((Player) sender).getLocation())) {
                        return DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);
                    } else {
                        sender.sendMessage("Not in the shopping area!");
                        return true;
                    }

                // a '/shop location' command
                } else {
                    if (DMWrapper.debugMode) DMWrapper.info("a /shop location command");

                    if (!DMWrapper.hasPermission(sender, "location")) {
                        sender.sendMessage("Not allowed to use the /shop location command!");
                        return true;
                    }

                    String pname = ((Player) sender).getName();

                    if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
                        sender.sendMessage("please right click the 1st corner");
                        DMWrapper.cmdMap.put(pname, "location set");
                        return true;
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
                        if (DMWrapper.cmdMap.get(pname) == null) {
                            sender.sendMessage("No operation to cancel");
                        } else {
                            sender.sendMessage("Operation canceled");
                            DMWrapper.cmdMap.remove(pname);
                        }
                        return true;
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("check")) {
                        Integer id = DMWrapper.locMgr.getShopID(((Player) sender).getLocation());
                        if (id > 0) {
                            sender.sendMessage("Shop ID: " + id);
                        } else {
                            sender.sendMessage("No shop location found here");
                        }
                        return true;
                    } else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
                        if (DMWrapper.locMgr.removeShopByID(Integer.parseInt(args[2]))) {
                            sender.sendMessage("Shop Location removed");
                        } else {
                            sender.sendMessage("Could not remove Shop ID: " + Integer.parseInt(args[2]));
                        }
                        return true;
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
                        sender.sendMessage("Shop Locations enabled");
                        DMWrapper.locMgr.enableShopLocations();
                        return true;
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
                        sender.sendMessage("Shop Locations disabled");
                        DMWrapper.locMgr.disableShopLocations();
                        return true;
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                        sender.sendMessage("Shop IDs: " + DMWrapper.locMgr.listShops());
                        return true;
                    } else if (args.length == 3 && args[1].equalsIgnoreCase("tp")) {
                        if (sender instanceof Player) {
                            Location dest = DMWrapper.locMgr.getCenterOfShop(Integer.parseInt(args[2]));
                            if (dest == null) {
                                sender.sendMessage("Could not find shop.");
                            } else {
                                ((Player) sender).teleportTo(dest);
                            }
                        }
                        return true;
                    } else {
                        showHelp(sender,"location");
                        return true;
                    }

                }
            }

        } else {
            return false;
        }

    }
    
    private boolean showHelp(CommandSender sender, String topic) {
        // TODO: Migrate help system to an MCDocs-like plugin eventually.
        Messaging message = new Messaging(sender);

        if (topic.isEmpty()) {
            String commands = "";
            String topics = "";
            String shortcuts = "";
            message.send("{}" + Misc.headerify("{CMD} " + DynamicMarket.name + " {BKT}({CMD}" + DynamicMarket.codename + "{BKT}){} "));
            message.send("{} {BKT}(){} Optional, {PRM}<>{} Parameter");
            message.send("{CMD} /shop help {BKT}({PRM}<topic/command>{BKT}){} - Show help.");
            message.send("{CMD} /shop {PRM}<id>{BKT}({CMD}:{PRM}<count>{BKT}){} - Show buy/sell info on an item.");
            message.send("{CMD} /shop {PRM}<command> <params>{} - Use a shop command.");
            commands += " list";
            shortcuts += " -? -l";

            if (DMWrapper.hasPermission(sender, "admin")) {
                commands += " location";
            }
            
            if (DMWrapper.dm.playerListener.hasPermission(sender, "buy")) {
                commands += " buy";
                shortcuts += " -b";
            }
            if (DMWrapper.dm.playerListener.hasPermission(sender, "sell")) {
                commands += " sell";
                shortcuts += " -s";
            }

            commands += " info";
            shortcuts += " -i";

            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.add")) {
                commands += " add";
                shortcuts += " -a";
            }
            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.update")) {
                commands += " update";
                shortcuts += " -u";
            }
            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.remove")) {
                commands += " remove";
                shortcuts += " -r";
            }
            if (DMWrapper.dm.playerListener.hasPermission(sender, "admin")) {
                commands += " reload";
                commands += " reset";
                commands += " exportdb importdb";
            }
            
            topics += "ids details about";
            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.add") || DMWrapper.dm.playerListener.hasPermission(sender, "items.update")) {
                topics += " tags";
            }

            message.send("{} Commands: {CMD}" + commands);
            message.send("{} Shortcuts: {CMD}" + shortcuts);
            message.send("{} Other help topics: {PRM}" + topics);
            return true;
        }
        message.send("{}" + Misc.headerify("{} " + DynamicMarket.name + " {BKT}({}" + DynamicMarket.codename + "{BKT}){} : " + topic + "{} "));
        if (topic.equalsIgnoreCase("buy")) {
            if (DMWrapper.dm.playerListener.hasPermission(sender, "buy")) {
                message.send("{CMD} /shop buy {PRM}<id>{BKT}({CMD}:{PRM}<count>{CMD})");
                message.send("{} Buy {PRM}<count>{} bundles of an item.");
                message.send("{} If {PRM}<count>{} is missing, buys 1 bundle.");
                return true;
            }
        }
        if (topic.equalsIgnoreCase("sell")) {
            if (DMWrapper.dm.playerListener.hasPermission(sender, "sell")) {
                message.send("{CMD} /shop sell {PRM}<id>{BKT}({CMD}:{PRM}<count>{CMD})");
                message.send("{} Sell {PRM}<count>{} bundles of an item.");
                message.send("{} If {PRM}<count>{} is missing, sells 1 bundle.");
                return true;
            }
        }
        if (topic.equalsIgnoreCase("info")) {
            // if (hasPermission(player,"sell"))
            // {
            message.send("{CMD} /shop info {PRM}<id>");
            message.send("{} Show detailed information about a shop item.");
            message.send("{} Unlike {CMD}/shop {PRM}<id>{}, this shows ALL fields.");
            return true;
            // }
        }
        if (topic.equalsIgnoreCase("add")) {
            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.add")) {
                message.send("{CMD} /shop add {PRM}<id>{BKT}({CMD}:{PRM}<bundle>{BKT}) ({PRM}<buyPrice>{BKT} ({PRM}<sellPrice>{BKT})) {PRM}<tags>");
                message.send("{} Adds item {PRM}<id>{} to the shop.");
                message.send("{} Transactions will be in {PRM}<bundle>{} units (default 1).");
                message.send("{PRM} <buyPrice>{} and {PRM}<sellPrice>{} will be converted, if used.");
                message.send("{} Prices are per-bundle.");
                message.send("{} See also: {CMD}/shop help tags");
                return true;
            }
        }
        if (topic.equalsIgnoreCase("update")) {
            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.update")) {
                message.send("{CMD} /shop update {PRM}<id>{BKT}({CMD}:{PRM}<bundle>{BKT}) ({PRM}<buyPrice>{BKT} ({PRM}<sellPrice>{BKT})) {PRM}<tags>");
                message.send("{} Changes item {PRM}<id>{}'s shop details.");
                message.send("{PRM} <bundle>{}, {PRM}<buyPrice>{}, {PRM}<sellPrice>{}, and {PRM}<tags>{} will be changed.");
                message.send("{} Transactions will be in {PRM}<bundle>{} units (default 1).");
                message.send("{} Prices are per-bundle.");
                message.send("{} See also: {CMD}/shop help tags");
                return true;
            }
        }
        if (topic.equalsIgnoreCase("remove")) {
            if (DMWrapper.dm.playerListener.hasPermission(sender, "items.remove")) {
                message.send("{CMD} /shop remove {PRM}<id>");
                message.send("{} Removes item {PRM}<id>{} from the shop.");
                return true;
            }
        }
        if (DMWrapper.dm.playerListener.hasPermission(sender, "admin")) {
            if (topic.equalsIgnoreCase("reload")) {
                message.send("{CMD} /shop reload");
                message.send("{} Restarts the shop plugin.");
                message.send("{} Attempts to reload all relevant config files.");
                return true;
            }
            if (topic.equalsIgnoreCase("reset")) {
                message.send("{CMD} /shop reset");
                message.send("{} Completely resets the shop database.");
                message.send("{} This will remove all items from the shop, and");
                message.send("{} create a new empty shop database.");
                return true;
            }
            if (topic.equalsIgnoreCase("exportdb")) {
                message.send("{CMD} /shop exportdb");
                message.send("{} Dumps the shop database to a .csv file.");
                message.send("{} Name and location are set in the main config file.");
                message.send("{} The file can be edited by most spreadsheet programs.");
                return true;
            }
            if (topic.equalsIgnoreCase("importdb")) {
                message.send("{CMD} /shop importdb");
                message.send("{} Reads a .csv file in to the shop database.");
                message.send("{} Name and location are set in the main config file.");
                message.send("{} The format MUST be the same as the export format.");
                message.send("{} Records matching id/subtype will be updated.");
                return true;
            }
        }
        if ( DMWrapper.hasPermission(sender, "admin") ) {
            if ( topic.equalsIgnoreCase("location") ) {
                // send player message on how to use /shop loc
                message.send("{CMD} /shop location set {BKT}- {}starts the shop setup process");
                message.send("{CMD} /shop location cancel {BKT}- {}cancels setting a shop location");
                message.send("{CMD} /shop location check {BKT}- {}checks ID of current location");
                message.send("{CMD} /shop location remove {PRM}<ID> {BKT}- {}removes the shop location");
                message.send("{CMD} /shop location enable {BKT}- {}enables location based shops");
                message.send("{CMD} /shop location disable {BKT}- {}disable location based shops");
                message.send("{CMD} /shop location list {BKT}- {}lists shop IDs");
                message.send("{CMD} /shop location tp {PRM}<ID> {BKT}- {}tps to shop");
                return true;
            }        
        }

        
        
        if (topic.equalsIgnoreCase("ids")) {
            message.send("{} Item ID format: {PRM}<id>{BKT}({CMD},{PRM}<subtype>{BKT})({CMD}:{PRM}<count>{BKT})");
            message.send("{PRM} <id>{}: Full name or ID number of the item.");
            message.send("{PRM} <subtype>{}: Subtype of the item (default: 0)");
            message.send("{} Subtypes are used for wool/dye colours, log types, etc.");
            message.send("{PRM} <count>{}: For shop items, this specifies bundle size.");
            message.send("{} For transactions, this sets the number of bundles bought or sold.");
            return true;
        }
        if (topic.equalsIgnoreCase("list")) {
            message.send("{CMD} /shop list {BKT}({PRM}<nameFilter>{BKT}) ({PRM}<page>{BKT})");
            message.send("{} Displays the items in the shop.");
            message.send("{} List format: {BKT}[{PRM}<id#>{BKT}]{PRM} <fullName> {BKT}[{PRM}<bundleSize>{BKT}]{} Buy {BKT}[{PRM}<buyPrice>{BKT}]{} Sell {BKT}[{PRM}<sellPrice>{BKT}]");
            message.send("{} Page 1 is displayed by default, if no page number is given.");
            message.send("{} If {PRM}<nameFilter>{} is used, displays items containing {PRM}<nameFilter>{}.");
            return true;
        }
        if (topic.equalsIgnoreCase("details")) {
            message.send("{CMD} /shop {PRM}<id>{BKT}({CMD}:{PRM}<count>{BKT})");
            message.send("{} Displays the current buy/sell price of the selected item.");
            message.send("{} Since prices can fluctuate, use {PRM}<count>{} to get batch pricing.");
            message.send("{} See {CMD}/shop help ids{} for information on IDs.");
            return true;
        }
        if ((Misc.isEither(topic.split(" ")[0], "tags", "tag")) && ((DMWrapper.dm.playerListener.hasPermission(sender, "items.add") || DMWrapper.dm.playerListener.hasPermission(sender, "items.update")))) {
            if (topic.indexOf(" ") > -1) {
                // Possible tag listed!
                String thisTag = topic.split(" ")[1].replace(":", "");
                if (Misc.isEither(thisTag, "n", "name")) {
                    message.send("{CMD} n:{BKT}|{CMD}name:{} - Name/rename item");
                    message.send("{} Sets the item's name in the shop DB.");
                    message.send("{} New name will persist until the item is removed.");
                    message.send("{} If name is blank, will try to reload the name from items.db.");
                    return true;
                }
                if (Misc.isEither(thisTag, "bp", "baseprice")) {
                    message.send("{CMD} bp:{BKT}|{CMD}BasePrice:{} - Base purchase price");
                    message.send("{} Buy price of the item at stock level 0.");
                    message.send("{} All other prices are derived from this starting value.");
                    message.send("{} Referenced by {PRM}SalesTax{}, {PRM}Stock{}, and {PRM}Volatility{}.");
                    message.send("{} Soft-limited by {PRM}PriceFloor{}/{PRM}PriceCeiling{}.");
                    return true;
                }
                if (Misc.isEither(thisTag, "s", "stock")) {
                    message.send("{CMD} s:{BKT}|{CMD}Stock:{} - Current stock level");
                    message.send("{} Stock level of this item (in bundles).");
                    message.send("{} Increases/decreases when items are sold/bought.");
                    message.send("{} Affects buy/sell prices, if {PRM}Volatility{} > 0.");
                    message.send("{} Soft-limited by {PRM}StockFloor{}/{PRM}StockCeiling{}.");
                    message.send("{} Hard-limited (transactions fail) by {PRM}StockLowest{}/{PRM}StockHighest{}.");
                    return true;
                }
                if (Misc.isEither(thisTag, "cb", "canbuy")) {
                    message.send("{CMD} cb:{BKT}|{CMD}CanBuy:{} - Buyability of item");
                    message.send("{} Set to 'Y', 'T', or blank to allow buying from shop.");
                    message.send("{} Set to 'N' or 'F' to disallow buying from shop.");
                    return true;
                }
                if (Misc.isEither(thisTag, "cs", "cansell")) {
                    message.send("{CMD} cs:{BKT}|{CMD}CanSell:{} - Sellability of item");
                    message.send("{} Set to 'Y', 'T', or blank to allow selling to shop.");
                    message.send("{} Set to 'N' or 'F' to disallow selling to shop.");
                    return true;
                }
                if (Misc.isAny(thisTag, new String[]{"v", "vol", "volatility"})) {
                    message.send("{CMD} v:{BKT}|{CMD}Vol:{}{BKT}|{CMD}Volatility:{} - Price volatility");
                    message.send("{} Percent increase in price per 1 bundle bought from shop, * 10000.");
                    message.send("{} v=0 prevents the price from changing with stock level.");
                    message.send("{} v=1 increases the price 1% per 100 bundles bought.");
                    message.send("{} v=10000 increases the price 100% per 1 bundle bought.");
                    message.send("{} Calculations are compound vs. current stock level.");
                    return true;
                }
                if (Misc.isAny(thisTag, new String[]{"iv", "ivol", "invvolatility"})) {
                    message.send("{CMD} iv:{BKT}|{CMD}IVol:{}{BKT}|{CMD}InvVolatility:{} - Inverse Volatility");
                    message.send("{} Number of bundles bought in order to double the price.");
                    message.send("{} Converted to volatility when entered.");
                    message.send("{} iv=+INF prevents the price from changing with stock level.");
                    message.send("{} iv=6400 doubles the price for each 6400 items bought.");
                    message.send("{} iv=1 doubles the price for each item bought.");
                    message.send("{} Calculations are compound vs. current stock level.");
                    return true;
                }
                if (Misc.isEither(thisTag, "st", "salestax")) {
                    message.send("{CMD} st:{BKT}|{CMD}SalesTax:{} - Sales Tax");
                    message.send("{} Percent difference between BuyPrice and SellPrice, * 100.");
                    message.send("{} {PRM}SellPrice{}={PRM}BuyPrice{}*(1-({PRM}SalesTax{}/100))");
                    message.send("{} If {PRM}SellPrice{} is entered as an untagged value, it is used to calculate {PRM}SalesTax{}.");
                    message.send("{} {PRM}SalesTax{} is applied after {PRM}PriceFloor{}/{PRM}PriceCeiling{}.");
                    return true;
                }
                if (Misc.isEither(thisTag, "sl", "stocklowest")) {
                    message.send("{CMD} sl:{BKT}|{CMD}StockLowest:{} - Lowest stock level (hard limit)");
                    message.send("{} Buying from shop will fail if it would put stock below {PRM}StockLowest{}.");
                    message.send("{} Set to 0 to to make stock 'finite'.");
                    message.send("{} Set to -INF or a negative value to use stock level as a 'relative offset'.");
                    return true;
                }
                if (Misc.isEither(thisTag, "sh", "stockhighest")) {
                    message.send("{CMD} sh:{BKT}|{CMD}StockHighest:{} - Highest stock level (hard limit)");
                    message.send("{} Selling to shop will fail if it would put stock above {PRM}StockHighest{}.");
                    message.send("{} Set to +INF to let maximum stock be unlimited.");
                    return true;
                }
                if (Misc.isEither(thisTag, "sf", "stockfloor")) {
                    message.send("{CMD} sf:{BKT}|{CMD}StockFloor:{} - Lowest stock level (soft limit)");
                    message.send("{} If {PRM}Stock{} falls below {PRM}StockFloor{}, it will be reset to {PRM}StockFloor{}.");
                    message.send("{} Further purchases will be at a flat rate, until {PRM}Stock{} rises.");
                    return true;
                }
                if (Misc.isEither(thisTag, "sc", "stockceiling")) {
                    message.send("{CMD} sc:{BKT}|{CMD}StockCeiling:{} - Highest stock level (soft limit)");
                    message.send("{} If {PRM}Stock{} rises above {PRM}StockCeiling{}, it will be reset to {PRM}StockCeiling{}.");
                    message.send("{} Further sales will be at a flat rate, until {PRM}Stock{} falls.");
                    return true;
                }
                if (Misc.isEither(thisTag, "pf", "pricefloor")) {
                    message.send("{CMD} pf:{BKT}|{CMD}PriceFloor:{} - Lowest buy price (soft limit)");
                    message.send("{} If {PRM}BuyPrice{} falls below {PRM}PriceFloor{}, it will be cropped at {PRM}PriceFloor{}.");
                    message.send("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} rises above {PRM}PriceFloor{}.");
                    message.send("{} {PRM}PriceFloor{} is applied to {PRM}SellPrice{} before {PRM}SalesTax{}.");
                    return true;
                }
                if (Misc.isEither(thisTag, "pc", "priceceiling")) {
                    message.send("{CMD} pc:{BKT}|{CMD}PriceCeiling:{} - Highest buy price (soft limit)");
                    message.send("{} If {PRM}BuyPrice{} rises above {PRM}PriceCeiling{}, it will be cropped at {PRM}PriceCeiling{}.");
                    message.send("{} Buy/sell prices will be at a flat rate, until {PRM}BuyPrice{} falls below {PRM}PriceCeiling{}.");
                    message.send("{} {PRM}PriceCeiling{} is applied to {PRM}SellPrice{} before {PRM}SalesTax{}.");
                    return true;
                }
                if (thisTag.equalsIgnoreCase("flat")) {
                    message.send("{CMD} flat{} - Set item with flat pricing.");
                    message.send("{} Buy/sell prices for this item will not change with stock level.");
                    message.send("{} Stock level WILL be tracked, and can float freely.");
                    message.send("{} Equivalent to: {CMD}s:0 sl:-INF sh:+INF sf:-INF sc:+INF v:0 pf:0 pc:+INF");
                    return true;
                }
                if (thisTag.equalsIgnoreCase("fixed")) {
                    message.send("{CMD} fixed{} - Set item with fixed pricing.");
                    message.send("{} Buy/sell prices for this item will not change with transactions.");
                    message.send("{} Stock level WILL NOT be tracked, and {PRM}Stock{} will remain at 0.");
                    message.send("{} Equivalent to: {CMD}s:0 sl:-INF sh:+INF sf:0 sc:0 v:0 pf:0 pc:+INF");
                    return true;
                }
                if (thisTag.equalsIgnoreCase("float")) {
                    message.send("{CMD} float{} - Set item with floating pricing.");
                    message.send("{} Buy/sell prices for this item will vary by stock level.");
                    message.send("{} If {PRM}Vol{}=0, {PRM}Vol{} will be set to a default of 100.");
                    message.send("{} (For finer control, set {PRM}Volatility{} to an appropriate value.)");
                    message.send("{} Stock level can float freely above and below 0 with transactions.");
                    message.send("{} Equivalent to: {CMD}sl:-INF sh:+INF sf:-INF sc:+INF {BKT}({CMD}v:100{BKT}){CMD} pf:0 pc:+INF");
                    return true;
                }
                if (thisTag.equalsIgnoreCase("finite")) {
                    message.send("{CMD} finite{} - Set item with finite stock.");
                    message.send("{} Buying from shop will fail if it would make {PRM}Stock{} < 0.");
                    message.send("{} Any number of items can be sold to the shop.");
                    message.send("{} Equivalent to: {CMD}sl:0 sh:+INF sf:-INF sc:+INF");
                    return true;
                }
                if (thisTag.equalsIgnoreCase("renorm")) {
                    message.send("{CMD} renorm{BKT}({CMD}:{PRM}<stock>{BKT}){} - Renormalize an item's price.");
                    message.send("{} Resets an item's {PRM}Stock{}, while preserving its current price.");
                    message.send("{} Sets an item's {PRM}BasePrice{} to its current {PRM}BuyPrice{},");
                    message.send("{} then sets {PRM}Stock{} to {PRM}<stock>{} (0 if blank or missing).");
                    return true;
                }
                message.send("{ERR} Unknown tag {PRM}" + thisTag + "{ERR}.");
                message.send("{ERR} Use {CMD}/shop help tags{ERR} to list tags.");
                return false;
            } else {
                message.send("{} Tag format: {PRM}<tagName>{BKT}({CMD}:{PRM}<value>{BKT}) ({PRM}<tagName>{BKT}({CMD}:{PRM}<value>{BKT}))...");
                message.send("{} Available tags: {CMD} Name: BasePrice: SalesTax: Stock: CanBuy: CanSell: Vol: IVol:");
                message.send("{CMD} StockLowest: StockHighest: StockFloor: StockCeiling: PriceFloor: PriceCeiling:");
                message.send("{} Available preset tags: {CMD}Fixed Flat Float Finite Renorm:");
                message.send("{} Use {CMD}/shop help tag {PRM}<tagName>{} for tag descriptions.");
                return true;
            }
        }
        if (topic.equalsIgnoreCase("about")) {
            message.send("{} " + DynamicMarket.name + " " + DynamicMarket.version + " written by HaloInverse.");
            message.send("{} Original structure and portions of code are from SimpleShop 1.1 by Nijikokun.");
            return true;
        }
        message.send("{}Unknown help topic:{CMD} " + topic);
        message.send("{}Use {CMD}/shop help{} to list topics.");
        return false;
    }
    

}
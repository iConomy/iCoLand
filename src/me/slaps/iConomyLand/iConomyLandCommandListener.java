package me.slaps.iConomyLand;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class iConomyLandCommandListener implements CommandExecutor {
	
    private static HashMap<String, String> cmdMap;
    private static HashMap<String, Cuboid> tmpCuboidMap;
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if ( iConomyLand.debugMode ) {
            String debug = "iConomyLand.onCommand(): " + ((sender instanceof Player) ? "Player " + ((Player) sender).getName() : "Console") + " Command " + cmd.getName() + " args: ";
            for (int i = 0; i < args.length; i++) 
                debug += args[i] + " ";
            iConomyLand.info(debug);
        }

        // temporary
//        if (!(sender instanceof Player))
//        	return false;

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
                    return true;
                } else {
                    showHelp(sender, args[1] );
                    return true;
                }
                
            // /icl list
            } else if (args[0].equalsIgnoreCase("list") ) {
                
                return true;
                
            // /icl list
            } else if (args[0].equalsIgnoreCase("select") ) {

                return true;
                
            // /icl list
            } else if (args[0].equalsIgnoreCase("survey") ) {

                return true;
                
            // /icl list
            } else if (args[0].equalsIgnoreCase("buy") ) {
                
                return true;
                
            // unrecognized /icl command
            } else {
                showHelp(sender, "invalid");
                return true;
            }

        // command not recognized ( not /icl )
        } else {
            return false;
        }

    }
    
    public void showHelp(CommandSender sender, String topic) {
        Messaging mess = new Messaging(sender);
        mess.send("{}" + Misc.headerify("{CMD} " + iConomyLand.name + " {BKT}({CMD}" + iConomyLand.codename + "{BKT}){} "));
    	if ( topic == null || topic.isEmpty() ) {
    	    
    	    mess.send(" {CMD}/icl {}- main command");
    	    mess.send(" {CMD}/icl help {PBK}[{PRM}topic{PBK}] {}- help topics");
    	    mess.send(" {CMD}/icl list {}- list owned land");
    	    mess.send(" {CMD}/icl select {}- select land");
    	    mess.send(" {CMD}/icl survey {}- get land info");
    	    mess.send(" {CMD}/icl buy {}- purchase selected land");
    	} else if ( topic.equalsIgnoreCase("invalid") ) {
    	    mess.send("{} unrecognized/invalid/malformed command");
    	    mess.send("{} Please use {CMD}/icl help {PBK}[{PRM}topic{PBK}] {}for help");
    	}
    }
    
    public boolean selectArea(Player player) {
        String playerName = player.getName();
        Messaging mess = new Messaging((CommandSender)player);
        
        if ( cmdMap.containsKey(playerName) ) {
            String action = cmdMap.get(playerName);
            if ( action.equals("set2") ) {
                mess.send("{ERR}Cancelling current selection process and starting over");
            } else {
                mess.send("{}1st Corner {PBK}({PRM}x,y,z{PBK}){}, please select the 2nd");
            }
            return true;
        } else {
            mess.send("{}Select 1st Corner");
            return true;
        }
    }
    

    
    
    
}

package me.slaps.iCoLand;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TaskTempBlockRemoval implements Runnable {
    
    Block target;
    Player player;
    Messaging mess;
    
    TaskTempBlockRemoval(Block target, Player player) {
        this.target = target;
        this.player = player;
        mess = new Messaging(player);
        mess.send("{}Temporary placement, returning item ID# "+target.getTypeId()+" in "+Config.tempItemDelay+ " seconds");
    }
    
    public void run() {
        if (Config.debugMode1)
            iCoLand.info("Removing item ID#"+target.getTypeId()+" placed by Player "+player.getName());
        
        PlayerInventory playerInv = player.getInventory();
        playerInv.addItem(new ItemStack(target.getTypeId(), 1));
        
        
        mess.send("{}Returning item ID# "+target.getTypeId());
        
        target.setType(Material.AIR);
    }

}

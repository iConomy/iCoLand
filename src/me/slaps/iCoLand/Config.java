package me.slaps.iCoLand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class Config {
    
    public static boolean loaded = false;
    
    // debug settings
    public static boolean debugMode;
    public static boolean debugMode1;
    public static boolean debugModeSQL;
    
    // database settings
    public static String h2DBFile = "lands.db";
    public static String sqlTableName = "lands";
    
    // import/export files
    public static String exportFile = "export.yml";
    public static String importFile = "lands.yml";

    // sales tax and periodic tax rate settings
    public static double sellTax;
    public static double taxRate;
    public static String bankName;
    public static Integer inactiveDeleteTime;
    
    // land limit settings
    public static Integer maxBlocksClaimable;
    public static Integer maxLandsClaimable;
    public static Integer minLandVolume;
    public static Integer maxLandVolume;
    
    // interval/timer settings
    public static Integer healTime;
    public static Integer mobRemovalTime;
    public static Integer taxTimeMinutes;
    public static Integer announceCheckInterval;
    
    // addons enabled
    public static HashMap<String, Boolean> addonsEnabled;
    
    // prices per block
    public static HashMap<String, Double> pricePerBlock;
    
    // unclaimed land options
    public static boolean unclaimedLandCanBuild;
    public static boolean unclaimedLandCanBoom;
    public static boolean unclaimedLandCanBurn;

    // claimed land settings
    public static ArrayList<Integer> tempItemsAllowed;
    public static Integer tempItemDelay;
    public static String defaultNoSpawnMobs;
    
    // selection options
    public static boolean allLandFullHeight;
    
    
    
    public static void getConfig(File dataFolder) {
        File configFile = new File(dataFolder + File.separator + "config.yml");

        // setup defaults
        debugMode = false;
        debugMode1 = false;
        debugModeSQL = false;
        
        sellTax = 0.80;
        taxRate = 0.05;
        inactiveDeleteTime = 1440;
        
        maxBlocksClaimable = 1000;
        maxLandsClaimable = 10;
        
        maxLandVolume = 1000;
        minLandVolume = 10;

        addonsEnabled = new HashMap<String, Boolean>();
        addonsEnabled.put("announce", true);
        addonsEnabled.put("noenter", true);
        addonsEnabled.put("heal", true);
        addonsEnabled.put("nospawn", true);
        addonsEnabled.put("noboom", true);
        addonsEnabled.put("nofire", true);
        addonsEnabled.put("noflow", true);
        addonsEnabled.put("nopvp", true);

        pricePerBlock = new HashMap<String, Double>();
        pricePerBlock.put("raw", 20.0);
        pricePerBlock.put("announce", 50.0);
        pricePerBlock.put("noenter", 100.0);
        pricePerBlock.put("heal", 200.0);
        pricePerBlock.put("nospawn", 50.0);
        pricePerBlock.put("noboom", 50.0);
        pricePerBlock.put("nofire", 10.0);
        pricePerBlock.put("noflow", 50.0);
        pricePerBlock.put("noflow", 10.0);
        
        healTime = 30;
        mobRemovalTime = 2;
        taxTimeMinutes = 0;
        announceCheckInterval = 1000;
        
        unclaimedLandCanBuild = true;
        unclaimedLandCanBoom = true;
        unclaimedLandCanBurn = true;
        
        tempItemsAllowed = new ArrayList<Integer>();
        tempItemsAllowed.add(65);
        tempItemsAllowed.add(50);
        tempItemDelay = 300;
        defaultNoSpawnMobs = "Spider,Zombie,Skeleton,Creeper,Slime,Ghast";
        
        allLandFullHeight = false;
        
        bankName = "iCoLand";

        
        // write default config file if it doesn't exist
        if ( !configFile.exists() ) {
            saveConfig(configFile);
        } else {
            loadConfig(configFile);
            saveConfig(configFile);
        }
        
        loaded = true;
    }
        
    public static void loadConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();
        
        
        ConfigurationNode debug = config.getNode("Debug");
        if ( debug != null ) {
            debugMode = debug.getBoolean("debug", false);
            debugMode1 = debug.getBoolean("debug-more-1", false);
            debugModeSQL = debug.getBoolean("debugSQL", false);
        }
        
        ConfigurationNode taxes = config.getNode("Tax-Settings");
        if ( taxes != null ) {
            sellTax = taxes.getDouble("SalesTaxPercent", 80.0)/100.0;
            if ( sellTax < 0 ) sellTax = 0;
            if ( sellTax > 1 ) sellTax = 1;
            
            taxRate = taxes.getDouble("TaxRate", 5.0)/100.0;
            if ( taxRate < 0 ) taxRate = 0;
            if ( taxRate > 1 ) taxRate = 1;
            
            bankName = taxes.getString("iConomy-Tax-Account-Name", "iCoLand");
            inactiveDeleteTime = taxes.getInt("Delete-Unpaid-Zone-Minutes", 1440);
        }

        ConfigurationNode timers = config.getNode("Timer-Settings");
        if ( timers != null ) {
            healTime = timers.getInt("Heal-Interval", 30);
            if ( healTime <= 0 ) healTime = 1;
            mobRemovalTime = timers.getInt("Mob-Removal-Interval", 2);
            taxTimeMinutes = timers.getInt("Tax-Interval-Minutes", 0);
            announceCheckInterval = timers.getInt("Announce-Check-Interval-ms", 1000);
        }
        
        ConfigurationNode unclaimed = config.getNode("Unclaimed-Land");
        if ( unclaimed != null ) {
            unclaimedLandCanBuild = unclaimed.getBoolean("Can-Build", true);
            unclaimedLandCanBoom = unclaimed.getBoolean("Can-Boom", true);
            unclaimedLandCanBurn = unclaimed.getBoolean("Can-Burn", true);
        }
        
        ConfigurationNode claimed = config.getNode("Claimed-Land");
        if ( claimed != null ) {
             String[] split = claimed.getString("Temp-Items-Allowed","50,65").split(",");
             tempItemsAllowed.clear();
             for(String item : split) {
                 try {
                     tempItemsAllowed.add(Integer.parseInt(item.trim()));
                 } catch (NumberFormatException ex ) {
                     iCoLand.warning("Could not parse Temp-Items-Allowed on token: "+item);
                 }
             }
             
             tempItemDelay = claimed.getInt("Temp-Items-Delay", 300);
             defaultNoSpawnMobs = claimed.getString("Default-No-Spawn-Mobs", "Spider,Zombie,Skeleton,Creeper,Slime,Ghast");
        }

        ConfigurationNode landLimits = config.getNode("Land-Limits");
        if ( landLimits != null ) {
            maxBlocksClaimable = landLimits.getInt("Max-Total-Blocks-Claimable", 1000);
            maxLandsClaimable = landLimits.getInt("Max-Lands-Claimable", 10);
            maxLandVolume = landLimits.getInt("Max-Land-Volume", 1000);
            minLandVolume = landLimits.getInt("Min-Land-Volume", 10);
        }
        
        addonsEnabled.clear();
        ConfigurationNode addons = config.getNode("Addons-Enabled");
        if ( addons != null ) {
            addonsEnabled.put("announce", addons.getBoolean("announce", false));
            addonsEnabled.put("noenter", addons.getBoolean("noenter", false));
            addonsEnabled.put("heal", addons.getBoolean("heal", false));
            addonsEnabled.put("nospawn", addons.getBoolean("nospawn", false));
            addonsEnabled.put("noboom", addons.getBoolean("noboom", false));
            addonsEnabled.put("nofire", addons.getBoolean("nofire", false));
            addonsEnabled.put("noflow", addons.getBoolean("noflow", false));
            addonsEnabled.put("nopvp", addons.getBoolean("nopvp", false));
        }

        pricePerBlock.clear();
        ConfigurationNode addonPrices = config.getNode("Price-Per-Block");
        if ( addonPrices != null ) {
            pricePerBlock.put("raw", addonPrices.getDouble("raw", 20.0));
            pricePerBlock.put("announce", addonPrices.getDouble("announce", 50.0));
            pricePerBlock.put("noenter", addonPrices.getDouble("noenter", 100.0));
            pricePerBlock.put("heal", addonPrices.getDouble("heal", 200.0));
            pricePerBlock.put("nospawn", addonPrices.getDouble("nospawn", 50.0));
            pricePerBlock.put("noboom", addonPrices.getDouble("noboom", 50.0));
            pricePerBlock.put("nofire", addonPrices.getDouble("nofire", 10.0));
            pricePerBlock.put("noflow", addonPrices.getDouble("noflow", 50.0));
            pricePerBlock.put("nopvp", addonPrices.getDouble("nopvp", 10.0));
        }
        
        ConfigurationNode selectionOptions = config.getNode("Selection-Options");
        if ( selectionOptions != null ) {
            allLandFullHeight = selectionOptions.getBoolean("All-Land-Full-Height", false);
        }
        

        
    }
    
    public static void saveConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        
        config.setProperty("Debug.debug", debugMode);
        config.setProperty("Debug.debug-more-1", debugMode1);
        config.setProperty("Debug.debugSQL", debugModeSQL);
        
        config.setProperty("Tax-Settings.SalesTaxPercent", sellTax*100.0);
        config.setProperty("Tax-Settings.TaxRate", taxRate*100.0);
        config.setProperty("Tax-Settings.iConomy-Tax-Account-Name", bankName);
        config.setProperty("Tax-Settings.Delete-Unpaid-Zone-Minutes", inactiveDeleteTime);
        
        config.setProperty("Timer-Settings.Heal-Interval", healTime);
        config.setProperty("Timer-Settings.Mob-Removal-Interval", mobRemovalTime);
        config.setProperty("Timer-Settings.Tax-Interval-Minutes", taxTimeMinutes);
        config.setProperty("Timer-Settings.Announce-Check-Interval-ms", announceCheckInterval);
        
        config.setProperty("Unclaimed-Land.Can-Build", unclaimedLandCanBuild);
        config.setProperty("Unclaimed-Land.Can-Boom", unclaimedLandCanBoom);
        config.setProperty("Unclaimed-Land.Can-Burn", unclaimedLandCanBurn);
        
        String tempItems = "";
        for(int i = 0; i<tempItemsAllowed.size(); i++) { 
            tempItems += tempItemsAllowed.get(i).toString(); 
            if ( !(i == tempItemsAllowed.size()-1) ) 
                tempItems += ",";
        }
        config.setProperty("Claimed-Land.Temp-Items-Allowed.", tempItems);
        config.setProperty("Claimed-Land.Temp-Items-Delay", tempItemDelay);
        config.setProperty("Claimed-Land.Default-No-Spawn-Mobs", defaultNoSpawnMobs);
        
        config.setProperty("Land-Limits.Max-Total-Blocks-Claimable", maxBlocksClaimable);
        config.setProperty("Land-Limits.Max-Lands-Claimable", maxLandsClaimable);
        config.setProperty("Land-Limits.Max-Land-Volume", maxLandVolume);
        config.setProperty("Land-Limits.Min-Land-Volume", minLandVolume);
        
        config.setProperty("Addons-Enabled.announce", addonsEnabled.get("announce"));
        config.setProperty("Addons-Enabled.noenter", addonsEnabled.get("noenter"));
        config.setProperty("Addons-Enabled.heal", addonsEnabled.get("heal"));
        config.setProperty("Addons-Enabled.nospawn", addonsEnabled.get("nospawn"));
        config.setProperty("Addons-Enabled.noboom", addonsEnabled.get("noboom"));
        config.setProperty("Addons-Enabled.nofire", addonsEnabled.get("nofire"));
        config.setProperty("Addons-Enabled.noflow", addonsEnabled.get("noflow"));
        config.setProperty("Addons-Enabled.nopvp", addonsEnabled.get("nopvp"));
        
        config.setProperty("Price-Per-Block.raw", pricePerBlock.get("raw"));
        config.setProperty("Price-Per-Block.announce", pricePerBlock.get("announce"));
        config.setProperty("Price-Per-Block.noenter", pricePerBlock.get("noenter"));
        config.setProperty("Price-Per-Block.heal", pricePerBlock.get("heal"));
        config.setProperty("Price-Per-Block.nospawn", pricePerBlock.get("nospawn"));
        config.setProperty("Price-Per-Block.noboom", pricePerBlock.get("noboom"));
        config.setProperty("Price-Per-Block.nofire", pricePerBlock.get("nofire"));
        config.setProperty("Price-Per-Block.noflow", pricePerBlock.get("noflow"));
        config.setProperty("Price-Per-Block.nopvp", pricePerBlock.get("nopvp"));
        
        config.setProperty("Selection-Options.All-Land-Full-Height", allLandFullHeight);

        config.save();
    }
    
    public static boolean isAddon(String addon) {
        return addonsEnabled.containsKey(addon);
    }
}

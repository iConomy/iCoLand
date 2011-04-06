package me.slaps.iCoLand;

import java.io.File;
import java.util.HashMap;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class Config {
    
    public static boolean loaded = false;
    
    public static boolean debugMode;
    public static boolean debugModeSQL = false;
    
    public static String h2DBFile = "lands.db";
    public static String sqlTableName = "lands";
    
    public static String exportFile = "export.yml";
    public static String importFile = "lands.yml";

    public static double sellTax;
    
    public static Integer maxBlocksClaimable;
    public static Integer maxLandsClaimable;
    public static Integer minLandVolume;
    public static Integer maxLandVolume;
    
    public static Integer healTime;
    public static Integer mobRemovalTime;
    
    public static double pricePerBlockRaw;
    public static HashMap<String, Boolean> addonsEnabled;
    public static HashMap<String, Double> addonsPricePerBlock;
    
    public static boolean preventGlobalBuildWithoutPerm;
    
    public static void getConfig(File dataFolder) {
        File configFile = new File(dataFolder + File.separator + "config.yml");
        
        // setup defaults
        debugMode = false;
        pricePerBlockRaw = 50.0;
        sellTax = 0.80;
        
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

        addonsPricePerBlock = new HashMap<String, Double>();
        addonsPricePerBlock.put("announce", 50.0);
        addonsPricePerBlock.put("noenter", 100.0);
        addonsPricePerBlock.put("heal", 200.0);
        addonsPricePerBlock.put("nospawn", 50.0);
        addonsPricePerBlock.put("noboom", 50.0);
        addonsPricePerBlock.put("nofire", 10.0);
        addonsPricePerBlock.put("noflow", 10.0);
        
        healTime = 30;
        mobRemovalTime = 2;
        
        preventGlobalBuildWithoutPerm = false;
        
        // write default config file if it doesn't exist
        if ( !configFile.exists() ) {
            saveConfig(configFile);
        } else {
            loadConfig(configFile);
            saveConfig(configFile);
        }
    }
        
    public static void loadConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();
        
        debugMode = config.getBoolean("debug", false);
        
        sellTax = config.getDouble("SalesTaxPercent", 80.0)/100.0;
        if ( sellTax < 0 ) sellTax = 0;
        if ( sellTax > 1 ) sellTax = 1;
        
        maxBlocksClaimable = config.getInt("Max-Total-Blocks-Claimable", 1000);
        maxLandsClaimable = config.getInt("Max-Lands-Claimable", 10);
        maxLandVolume = config.getInt("Max-Land-Volume", 1000);
        minLandVolume = config.getInt("Min-Land-Volume", 10);

        pricePerBlockRaw = config.getDouble("PricePerBlock-Raw", 20.0);        
        
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
        } else {
            addonsEnabled.put("announce", false);
            addonsEnabled.put("noenter", false);
            addonsEnabled.put("heal", false);
            addonsEnabled.put("nospawn", false);
            addonsEnabled.put("noboom", false);
            addonsEnabled.put("nofire", false);
            addonsEnabled.put("noflow", false);
            
        }

        addonsPricePerBlock.clear();
        ConfigurationNode addonPrices = config.getNode("Addons-PricePerBlock");
        if ( addonPrices != null ) {
            addonsPricePerBlock.put("announce", addonPrices.getDouble("announce", 50.0));
            addonsPricePerBlock.put("noenter", addonPrices.getDouble("noenter", 100.0));
            addonsPricePerBlock.put("heal", addonPrices.getDouble("heal", 200.0));
            addonsPricePerBlock.put("nospawn", addonPrices.getDouble("nospawn", 50.0));
            addonsPricePerBlock.put("noboom", addonPrices.getDouble("noboom", 50.0));
            addonsPricePerBlock.put("nofire", addonPrices.getDouble("nofire", 10.0));
            addonsPricePerBlock.put("noflow", addonPrices.getDouble("noflow", 50.0));
        } else {
            addonsPricePerBlock.put("announce", 50.0);
            addonsPricePerBlock.put("noenter", 100.0);
            addonsPricePerBlock.put("heal", 200.0);
            addonsPricePerBlock.put("nospawn", 50.0);
            addonsPricePerBlock.put("noboom", 50.0);
            addonsPricePerBlock.put("nofire", 10.0);
            addonsPricePerBlock.put("noflow", 50.0);
        }
        
        healTime = config.getInt("Heal-Interval", 30);
        mobRemovalTime = config.getInt("Mob-Removal-Interval", 2);
        
        preventGlobalBuildWithoutPerm = config.getBoolean("Prevent-Build-Without-Perm", false);
        
        loaded = true;
    }
    
    public static void saveConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.setProperty("debug", debugMode);
        config.setProperty("PricePerBlock-Raw", pricePerBlockRaw);
        config.setProperty("SalesTaxPercent", sellTax*100.0);
        
        config.setProperty("Max-Total-Blocks-Claimable", maxBlocksClaimable);
        config.setProperty("Max-Lands-Claimable", maxLandsClaimable);
        config.setProperty("Max-Land-Volume", maxLandVolume);
        config.setProperty("Min-Land-Volume", minLandVolume);
        
        config.setProperty("Addons-Enabled.announce", addonsEnabled.get("announce"));
        config.setProperty("Addons-Enabled.noenter", addonsEnabled.get("noenter"));
        config.setProperty("Addons-Enabled.heal", addonsEnabled.get("heal"));
        config.setProperty("Addons-Enabled.nospawn", addonsEnabled.get("nospawn"));
        config.setProperty("Addons-Enabled.noboom", addonsEnabled.get("noboom"));
        config.setProperty("Addons-Enabled.nofire", addonsEnabled.get("nofire"));
        config.setProperty("Addons-Enabled.noflow", addonsEnabled.get("noflow"));
        
        config.setProperty("Addons-PricePerBlock.announce", addonsPricePerBlock.get("announce"));
        config.setProperty("Addons-PricePerBlock.noenter", addonsPricePerBlock.get("noenter"));
        config.setProperty("Addons-PricePerBlock.heal", addonsPricePerBlock.get("heal"));
        config.setProperty("Addons-PricePerBlock.nospawn", addonsPricePerBlock.get("nospawn"));
        config.setProperty("Addons-PricePerBlock.noboom", addonsPricePerBlock.get("noboom"));
        config.setProperty("Addons-PricePerBlock.nofire", addonsPricePerBlock.get("nofire"));
        config.setProperty("Addons-PricePerBlock.noflow", addonsPricePerBlock.get("noflow"));
        
        config.setProperty("Heal-Interval", healTime);
        config.setProperty("Mob-Removal-Interval", mobRemovalTime);
        
        config.setProperty("Prevent-Build-Without-Perm", preventGlobalBuildWithoutPerm);

        config.save();
    }
    
    public static boolean isAddon(String addon) {
        return addonsEnabled.containsKey(addon);
    }
}

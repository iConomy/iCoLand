package me.slaps.iCoLand;

import java.io.File;
import java.util.HashMap;

import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class Config {
    
    public static boolean debugMode;

    public static double sellTax;
    
    public static Integer maxBlocksClaimable;
    public static Integer maxLandsClaimable;
    public static Integer minLandVolume;
    public static Integer maxLandVolume;
    
    public static Integer healTime;
    
    public static double pricePerBlockRaw;
    public static HashMap<String, Boolean> addonsEnabled;
    public static HashMap<String, Double> addonsPricePerBlock;
    
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

        addonsPricePerBlock = new HashMap<String, Double>();
        addonsPricePerBlock.put("announce", 50.0);
        addonsPricePerBlock.put("noenter", 100.0);
        addonsPricePerBlock.put("heal", 200.0);
        addonsPricePerBlock.put("nospawn", 50.0);
        
        healTime = 200;
        
        // write default config file if it doesn't exist
        if ( !configFile.exists() ) {
            saveConfig(configFile);
        }
        
        loadConfig(configFile);
        saveConfig(configFile);
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
        addonsEnabled.put("announce", addons.getBoolean("announce", false));
        addonsEnabled.put("noenter", addons.getBoolean("noenter", false));
        addonsEnabled.put("heal", addons.getBoolean("heal", false));
        addonsEnabled.put("nospawn", addons.getBoolean("nospawn", false));

        addonsPricePerBlock.clear();
        ConfigurationNode addonPrices = config.getNode("Addons-PricePerBlock");
        addonsPricePerBlock.put("announce", addonPrices.getDouble("announce", 50.0));
        addonsPricePerBlock.put("noenter", addonPrices.getDouble("noenter", 100.0));
        addonsPricePerBlock.put("heal", addonPrices.getDouble("heal", 200.0));
        addonsPricePerBlock.put("nospawn", addonPrices.getDouble("nospawn", 50.0));
        
        healTime = config.getInt("Heal-Interval", 200);
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

        config.setProperty("Addons-PricePerBlock.announce", addonsPricePerBlock.get("announce"));
        config.setProperty("Addons-PricePerBlock.noenter", addonsPricePerBlock.get("noenter"));
        config.setProperty("Addons-PricePerBlock.heal", addonsPricePerBlock.get("heal"));
        config.setProperty("Addons-PricePerBlock.nospawn", addonsPricePerBlock.get("nospawn"));
        
        config.setProperty("Heal-Interval", healTime);

        config.save();
    }
    
    public static boolean isAddon(String addon) {
        return addonsEnabled.containsKey(addon);
    }
}

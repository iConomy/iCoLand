package me.slaps.iConomyLand;

import java.io.File;

import org.bukkit.util.config.Configuration;

public class Config {
    
    public static boolean debugMode = false;

    public static double pricePerBlockRaw;
    public static double pricePerBlockAddonAnnounce;
    public static double pricePerBlockAddonHealing;
    public static double pricePerBlockAddonNoEnter;
    public static double sellTax;
    
    public static void getConfig(File dataFolder) {
        File configFile = new File(dataFolder + File.separator + "config.yml");
        
        // write default config file if it doesn't exist
        if ( !configFile.exists() ) {
            saveConfig(configFile);
        }
        
        loadConfig(configFile);
    }
        
    public static void loadConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();
        debugMode = config.getBoolean("debug", false);
        pricePerBlockRaw = config.getDouble("PricePerBlock-Raw", 20.0);
        pricePerBlockAddonAnnounce = config.getDouble("PricePerBlock-Addon-Announce", 50.0);
        pricePerBlockAddonHealing = config.getDouble("PricePerBlock-Addon-Healing", 200.0);
        pricePerBlockAddonNoEnter = config.getDouble("PricePerBlock-Addon-NoEnter", 200.0);
        sellTax = config.getDouble("SalesTaxPercent", 80.0)/100.0;
        if ( sellTax < 0 ) sellTax = 0;
        if ( sellTax > 1 ) sellTax = 1;
    }
    
    public static void saveConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.setProperty("debug", debugMode);
        config.setProperty("PricePerBlock-Raw", pricePerBlockRaw);
        config.setProperty("PricePerBlock-Addon-Announce", pricePerBlockAddonAnnounce);
        config.setProperty("PricePerBlock-Addon-Healing", pricePerBlockAddonHealing);
        config.setProperty("PricePerBlock-Addon-NoEnter", pricePerBlockAddonNoEnter);
        config.setProperty("SalesTaxPercent", sellTax*100.0);
        config.save();
    }
}

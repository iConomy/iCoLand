package me.slaps.iCoLand;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;
import org.h2.jdbcx.JdbcConnectionPool;

public class LandDBH2 extends LandDB {
    
    private String dbPath;
    
    private static JdbcConnectionPool cp;

    
    public LandDBH2(String pathToDB) {
        super();
        dbPath = pathToDB;
        
        initDB();
        load();
    }
    
    public void initDB() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        cp = JdbcConnectionPool.create("jdbc:h2:"+dbPath+";AUTO_RECONNECT=TRUE", "sa", "sa");
        if ( !tableExists(Config.sqlTableName) ) {
            createTable();
        }
    }
    
    public void close() {
        cp.dispose();
    }
    
    
    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = cp.getConnection();
        } catch( SQLException ex ) {
            ex.printStackTrace();
            iCoLand.severe("Could not connect to database!");
        }
        return conn;
    }

    public boolean tableExists(String table) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            iCoLand.info("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+table.toUpperCase()+"';");
            ps = conn.prepareStatement("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+table.toUpperCase()+"';");
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString(1).equals(table.toUpperCase())) {
                    ps.close();
                    conn.close();
                    return true;
                }
            }
            
            ps.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
        
        
    public void createTable() {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            iCoLand.info("CREATE TABLE IF NOT EXISTS " + Config.sqlTableName + "("+
                    "id INT auto_increment PRIMARY KEY,"+
                    "owner VARCHAR(32),"+
                    "minX INT,"+
                    "minY INT,"+
                    "minZ INT,"+
                    "maxX INT,"+
                    "maxY INT,"+
                    "maxZ INT,"+
                    "dateCreated TIMESTAMP,"+
                    "dateTaxed TIMESTAMP,"+
                    "name VARCHAR(32),"+
                    "perms VARCHAR(256),"+
                    "addons VARCHAR(256)"+
                    ");");
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + Config.sqlTableName + "("+
                    "id INT auto_increment PRIMARY KEY,"+
                    "owner VARCHAR(32),"+
                    "minX INT,"+
                    "minY INT,"+
                    "minZ INT,"+
                    "maxX INT,"+
                    "maxY INT,"+
                    "maxZ INT,"+
                    "dateCreated TIMESTAMP,"+
                    "dateTaxed TIMESTAMP,"+
                    "name VARCHAR(32),"+
                    "perms VARCHAR(256),"+
                    "addons VARCHAR(256)"+
                    ");");
            ps.executeUpdate();
            ps.close();
            conn.close();
        } catch(SQLException ex) { 
            ex.printStackTrace();
        }

    }


    public void load() {
//        //LandConfig.load();
//        
//        //List<String> oList = LandConfig.getStringList("lands", null);
//        
//        iCoLand.warning("Found " + oList.size() + " lands to protect ( loaded from file )");
//        
//        Iterator<String> itr = oList.iterator();
//        while(itr.hasNext()) {
//            String o = itr.next();
//            
//            LinkedHashMap<String,String> shopkeys = new LinkedHashMap<String,String>();
//            
//            String[] split = o.replaceFirst("\\{(.*)\\}","$1").split(",");
//            for( String line : split ) {
//                String[] ls = line.trim().split("=");
//                shopkeys.put(ls[0].trim(), (ls.length>1)?ls[1].trim():"");
//            }
//
//            int id = Integer.parseInt(shopkeys.get("id"));
//            
//            Location loc1 = new Location(iCoLand.server.getWorld(shopkeys.get("world")), 
//                                         Double.parseDouble(shopkeys.get("corner1x")), 
//                                         Double.parseDouble(shopkeys.get("corner1y")), 
//                                         Double.parseDouble(shopkeys.get("corner1z")) );
//            Location loc2 = new Location(iCoLand.server.getWorld(shopkeys.get("world")), 
//                                         Double.parseDouble(shopkeys.get("corner2x")), 
//                                         Double.parseDouble(shopkeys.get("corner2y")), 
//                                         Double.parseDouble(shopkeys.get("corner2z")) );
//            Cuboid loc = new Cuboid(loc1, loc2);
//            String owner = shopkeys.get("owner");
//            HashMap<String, Boolean> perms = Land.parsePermTags(shopkeys.get("perms"));
//            HashMap<String, Boolean> addons = Land.parseAddonTags(shopkeys.get("addons"));
//            String dateCreated = shopkeys.get("dateCreated");
//            String dateTaxed = shopkeys.get("dateTaxed");
//            String locationName = shopkeys.get("name");
//
//            lands.put(id, new Land(id, loc, owner, locationName, perms, addons, dateCreated, dateTaxed));
//
//        }
//        
//        save();
        
    }
    
    public void save() {
//        LandConfig = new Configuration(landConfigFile);
//        
//        ArrayList<LinkedHashMap<String,Object>> tmpshops = new ArrayList<LinkedHashMap<String,Object>>();
//        Iterator<Land> itr = lands.values().iterator();
//        while(itr.hasNext()) {
//            Land land = itr.next();
//            LinkedHashMap<String,Object> tmpmap = new LinkedHashMap<String,Object>();
//            
//            tmpmap.put("id", land.getID());
//            tmpmap.put("owner", land.owner);
//            tmpmap.put("perms", Land.writePermTags(land.canBuildDestroy));
//            tmpmap.put("addons", Land.writeAddonTags(land.addons));
//            tmpmap.put("dateCreated", land.dateCreated.toString() );
//            tmpmap.put("dateTaxed", land.dateTaxed.toString() );
//            tmpmap.put("name", land.locationName);
//            tmpmap.put("world", land.location.setLoc1.getWorld().getName());
//            tmpmap.put("corner1x",land.location.setLoc1.getBlockX());
//            tmpmap.put("corner1y",land.location.setLoc1.getBlockY());
//            tmpmap.put("corner1z",land.location.setLoc1.getBlockZ());
//            tmpmap.put("corner2x",land.location.setLoc2.getBlockX());
//            tmpmap.put("corner2y",land.location.setLoc2.getBlockY());
//            tmpmap.put("corner2z",land.location.setLoc2.getBlockZ());
//
//            tmpshops.add(tmpmap);           
//        }
//        LandConfig.setProperty("lands", tmpshops);
//        
//        LandConfig.save();
    }

    @Override
    public int createNewLand(Land newLand) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean removeLandById(int id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Land[] listAllLand() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Land[] listLandOwnerBy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLandId(Location loc) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Land getLandById(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLandPerms(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLandAddons(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLandOwner(int id) {
        // TODO Auto-generated method stub
        return null;
    }
    

    
    
    
    
    
}

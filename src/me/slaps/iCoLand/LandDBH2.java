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

import me.slaps.iCoLand.Land;
import me.slaps.iCoLand.LandDB;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;
import org.h2.jdbcx.JdbcConnectionPool;

public class LandDBH2 implements LandDB {
    
    private String dbPath;
    private static JdbcConnectionPool cp;
    
    public LandDBH2(String pathToDB) {
        dbPath = pathToDB;
        
        initDB();
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

    public boolean createNewLand(Land newLand) {
        int ret = 0;
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = getConnection();
    		ps = conn.prepareStatement("INSERT INTO "+Config.sqlTableName+
    		        "( owner, dateCreated, dateTaxed, name, perms, addons, "+
    		        "minX, minY, minZ, maxX, maxY, maxZ) " +
    		        "VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"+
                    ";");
    		ps.setString(1, newLand.owner);
    		ps.setTimestamp(2, newLand.dateCreated);
    		ps.setTimestamp(3, newLand.dateTaxed);
    		ps.setString(4, newLand.locationName);
    		ps.setString(5, Land.writePermTags(newLand.canBuildDestroy));
    		ps.setString(6, Land.writeAddonTags(newLand.addons));
    		ps.setInt(7, newLand.location.LocMin.getBlockX());
    		ps.setInt(8, newLand.location.LocMin.getBlockY());
    		ps.setInt(9, newLand.location.LocMin.getBlockZ());
    		ps.setInt(10, newLand.location.LocMax.getBlockX());
    		ps.setInt(11, newLand.location.LocMax.getBlockY());
    		ps.setInt(12, newLand.location.LocMax.getBlockZ());
    		ret = ps.executeUpdate();
    		
    	} catch ( SQLException ex ) {
    	    ex.printStackTrace();
    	}
    	return (ret>0)?true:false;
    }

    public boolean removeLandById(int id) {
        // TODO Auto-generated method stub
        return false;
    }

    public ArrayList<Land> listAllLand() {
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<Land> listLandOwnedBy(String playerName) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLandId(Location loc) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Land getLandById(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLandPerms(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLandAddons(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLandOwner(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean updateLandOwner(int id, String newOwner) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean updateLandName(int id, String newName) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean updateLandPerms(int id, HashMap<String, Boolean> newPerms) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean updateLandAddons(int id, HashMap<String, Boolean> newAddons) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasPermission(int id, String playerName) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean landIdExists(int id) {
        // TODO Auto-generated method stub
        return false;
    }

    public int intersectsExistingLand(Cuboid loc) {
        // TODO Auto-generated method stub
        return 0;
    }
    
}

package me.slaps.iCoLand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import me.slaps.iCoLand.Land;
import me.slaps.iCoLand.LandDB;

import org.bukkit.Location;
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
        boolean ret = false;
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            iCoLand.info("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+table.toUpperCase()+"';");
            ps = conn.prepareStatement("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"+table.toUpperCase()+"';");
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString(1).equals(table.toUpperCase())) {
                    ret = true;
                }
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }
        
        
    public void createTable() {
        Connection conn = getConnection();
        Statement st = null;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + Config.sqlTableName + "("+
                "id INT auto_increment PRIMARY KEY,"+
                "owner VARCHAR(32),"+
                "world VARCHAR(32),"+
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
                ");";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MinMinMin ON " + Config.sqlTableName + " (minX, minY, minZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MinMinMax ON " + Config.sqlTableName + " (minX, minY, maxZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MinMaxMin ON " + Config.sqlTableName + " (minX, maxY, minZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MinMaxMax ON " + Config.sqlTableName + " (minX, maxY, maxZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MaxMinMin ON " + Config.sqlTableName + " (maxX, minY, minZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MaxMinMax ON " + Config.sqlTableName + " (maxX, minY, maxZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MaxMaxMin ON " + Config.sqlTableName + " (maxX, maxY, minZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
            sql = "CREATE INDEX MaxMaxMax ON " + Config.sqlTableName + " (maxX, maxY, maxZ);";
            st = conn.createStatement();
            iCoLand.info(sql);
            st.executeUpdate(sql);
            st.close();
            
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
    		        "minX, minY, minZ, maxX, maxY, maxZ, world) " +
    		        "VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"+
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
    		ps.setString(13, newLand.location.LocMax.getWorld().getName());
    		ret = ps.executeUpdate();
    		
    		ps.close();
    		conn.close();
    		
    	} catch ( SQLException ex ) {
    	    ex.printStackTrace();
    	}
    	return (ret>0)?true:false;
    }

    public boolean removeLandById(int id) {
        int ret = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM "+Config.sqlTableName+
                    " WHERE id = ?;");
            ps.setInt(1, id);
            ret = ps.executeUpdate();
            
            ps.close();
            conn.close();
            
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        return (ret>0)?true:false;
    }

    public ArrayList<Land> listAllLand() {
        return listLandOwnedBy(null);
    }

    public ArrayList<Land> listLandOwnedBy(String playerName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT id, owner, dateCreated, dateTaxed, name, perms, addons, "+
                    "minX, minY, minZ, maxX, maxY, maxZ, world FROM "+Config.sqlTableName+
                    ((playerName != null)?" WHERE owner = ?":"")+
                    " ORDER BY id ASC;");
            if ( playerName != null ) ps.setString(1, playerName);
            rs = ps.executeQuery();
            
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        ArrayList<Land> ret = new ArrayList<Land>();
        
        try {
            rs.beforeFirst();
            while( rs.next() ) {
                Location locMin = new Location(iCoLand.server.getWorld(rs.getString(14)), rs.getInt(8), rs.getInt(9), rs.getInt(10));
                Location locMax = new Location(iCoLand.server.getWorld(rs.getString(14)), rs.getInt(11), rs.getInt(12), rs.getInt(13));
                Cuboid newCub = new Cuboid(locMin, locMax);
                
                ret.add(new Land(rs.getInt(1), newCub, rs.getString(2), rs.getString(5), Land.parsePermTags(rs.getString(6)), 
                                 Land.parseAddonTags(rs.getString(7)), rs.getTimestamp(3), rs.getTimestamp(4)
                                ));
            }
            
            rs.close();
            ps.close();
            conn.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        return ret;
    }

    public int getLandId(Location loc) {
        int ret = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT id FROM "+Config.sqlTableName+
                    " WHERE ? BETWEEN minX AND maxX AND ? BETWEEN minY AND maxY AND ? BETWEEN minZ AND maxZ"+
                    " ORDER BY id ASC;");
            ps.setInt(1, loc.getBlockX());
            ps.setInt(2, loc.getBlockY());
            ps.setInt(3, loc.getBlockZ());
            rs = ps.executeQuery();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        try {
            int i = 0;
            while ( rs.next() ) {
                i++;
                if ( i > 1 ) 
                    iCoLand.warning("more than 1 land found");
                ret = rs.getInt(1);
            }
            
            rs.close();
            ps.close();
            conn.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        return ret;
    }
    
    public int containsLandId(Cuboid cub) {
        int ret = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> sqls = new ArrayList<String>();
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE minX BETWEEN ? AND ? AND minY BETWEEN ? AND ? AND minZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE minX BETWEEN ? AND ? AND minY BETWEEN ? AND ? AND maxZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE minX BETWEEN ? AND ? AND maxY BETWEEN ? AND ? AND minZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE minX BETWEEN ? AND ? AND maxY BETWEEN ? AND ? AND maxZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE maxX BETWEEN ? AND ? AND minY BETWEEN ? AND ? AND minZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE maxX BETWEEN ? AND ? AND minY BETWEEN ? AND ? AND maxZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                 " WHERE maxX BETWEEN ? AND ? AND maxY BETWEEN ? AND ? AND minZ BETWEEN ? AND ?"+
                 " ORDER BY id ASC;");
        sqls.add("SELECT id FROM "+Config.sqlTableName+
                " WHERE maxX BETWEEN ? AND ? AND maxY BETWEEN ? AND ? AND maxZ BETWEEN ? AND ?"+
                  " ORDER BY id ASC;");
        try {
            conn = getConnection();
            for(String sql : sqls) {
                ps = conn.prepareStatement(sql);
                ps.setInt(1,cub.LocMin.getBlockX());
                ps.setInt(2,cub.LocMax.getBlockX());
                ps.setInt(3,cub.LocMin.getBlockY());
                ps.setInt(4,cub.LocMax.getBlockY());
                ps.setInt(5,cub.LocMin.getBlockZ());
                ps.setInt(6,cub.LocMax.getBlockZ());
                rs = ps.executeQuery();
                
                while ( rs.next() ) {
                    ret = rs.getInt(1);
                    ps.close();
                    rs.close();
                    conn.close();
                    break;
                }
                ps.close();
                rs.close();
            }
            conn.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        return ret;
    }
    
    public int intersectsExistingLand(Cuboid cub) {
        int id = 0;
        id = containsLandId(cub);
        if ( id > 0 ) return id;
        
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMin.getBlockX(), cub.LocMin.getBlockY(), cub.LocMin.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMin.getBlockX(), cub.LocMin.getBlockY(), cub.LocMax.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMin.getBlockX(), cub.LocMax.getBlockY(), cub.LocMin.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMin.getBlockX(), cub.LocMax.getBlockY(), cub.LocMax.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMax.getBlockX(), cub.LocMin.getBlockY(), cub.LocMin.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMax.getBlockX(), cub.LocMin.getBlockY(), cub.LocMax.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMax.getBlockX(), cub.LocMax.getBlockY(), cub.LocMin.getBlockZ()));
        if ( id > 0 ) return id;
        id = getLandId(new Location(cub.LocMin.getWorld(), 
                cub.LocMax.getBlockX(), cub.LocMax.getBlockY(), cub.LocMax.getBlockZ()));
        if ( id > 0 ) return id;
        
        return 0;
    }

    public Land getLandById(int id) {
        Land ret = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT id, owner, dateCreated, dateTaxed, name, perms, addons, "+
                    "minX, minY, minZ, maxX, maxY, maxZ, world FROM "+Config.sqlTableName+
                    " WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        try {
            int i = 0;
            while ( rs.next() ) {
                i++;
                if ( i > 1 ) 
                    iCoLand.warning("More than 1 Land with same ID!");
                
                Location locMin = new Location(iCoLand.server.getWorld(rs.getString(14)), rs.getInt(8), rs.getInt(9), rs.getInt(10));
                Location locMax = new Location(iCoLand.server.getWorld(rs.getString(14)), rs.getInt(11), rs.getInt(12), rs.getInt(13));
                Cuboid newCub = new Cuboid(locMin, locMax);
                    
                ret = new Land(rs.getInt(1), newCub, rs.getString(2), rs.getString(5), Land.parsePermTags(rs.getString(6)), 
                               Land.parseAddonTags(rs.getString(7)), rs.getTimestamp(3), rs.getTimestamp(4)
                              );
            }
                

                
            rs.close();
            ps.close();
            conn.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return ret;
    }
    
    public boolean landIdExists(int id) {
        Land land = getLandById(id);
        return ( land != null );
    }
    
    public String getLandPerms(int id) {
        String ret = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT perms FROM "+Config.sqlTableName+
                    " WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        try {
            while( rs.next() ) {
                ret = rs.getString(1);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        return ret;
    }

    public String getLandAddons(int id) {
        String ret = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT addons FROM "+Config.sqlTableName+
                    " WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        try {
            while( rs.next() ) {
                ret = rs.getString(1);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        return ret;
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




    
}

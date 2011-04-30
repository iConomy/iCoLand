package me.slaps.iCoLand;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Achievement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class OfflinePlayer implements Player {
    private Location location;
    private World world;
    private int entId = -1;
    String name;
    private String displayName;
    
    public OfflinePlayer(Server server, World world, String name) {
        displayName = this.name = name;
        this.world = world; // TEMP!
        location = world.getSpawnLocation(); // TEMP!
        //ServerConfigurationManager confmgr = ((CraftServer)server).getHandle();
        //File worldFile = ((CraftWorld)world).getHandle().u;
        //PlayerNBTManager pnm = confmgr.
        //PlayerNBTManager pnm = new PlayerNBTManager(worldFile , name, false);
        //new File(worldFile, "_tmp_.dat");
    }

    public String getName() {
        return name;
    }

    public PlayerInventory getInventory() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public ItemStack getItemInHand() {
        return getInventory().getItemInHand();
    }

    public void setItemInHand(ItemStack item) {
        getInventory().setItemInHand(item);
    }

    public int getHealth() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setHealth(int health) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public double getEyeHeight() {
        return getEyeHeight(false);
    }

    public double getEyeHeight(boolean ignoreSneaking) {
        if(ignoreSneaking) {
            return 1.62D;
        } else {
            if (isSneaking()) {
                return 1.42D;
            } else {
                return 1.62D;
            }
        }
    }

    public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 0);
    }

    private List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance, int maxLength) {
        if (maxDistance > 120) {
            maxDistance = 120;
        }
        ArrayList<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(this, maxDistance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (maxLength != 0 && blocks.size() > maxLength) {
                blocks.remove(0);
            }
            int id = block.getTypeId();
            if (transparent == null) {
                if (id != 0) {
                    break;
                }
            } else {
                if (!transparent.contains((byte)id)) {
                    break;
                }
            }
        }
        return blocks;
    }

    public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
        List<Block> blocks = getLineOfSight(transparent, maxDistance, 1);
        return blocks.get(0);
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 2);
    }

    public Egg throwEgg() {
        throw new UnsupportedOperationException("Player is offline");
    }

    public Snowball throwSnowball() {
        throw new UnsupportedOperationException("Player is offline");
    }

    public Arrow shootArrow() {
        throw new UnsupportedOperationException("Player is offline");
    }

    public boolean isInsideVehicle() {
        return false;
    }

    public boolean leaveVehicle() {
        throw new UnsupportedOperationException("Player is offline");
    }

    public Vehicle getVehicle() {
        return null;
    }

    public int getRemainingAir() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setRemainingAir(int ticks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public int getMaximumAir() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setMaximumAir(int ticks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Location getLocation() {
        return location;
    }

    public World getWorld() {
        return world;
    }

    public void teleportTo(Location location) {
        teleport(location);
    }

    public void teleportTo(Entity destination) {
        teleport(destination);
    }

    public int getEntityId() {
        return entId ;
    }

    public int getFireTicks() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public int getMaxFireTicks() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setFireTicks(int ticks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void remove() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Server getServer() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void sendMessage(String message) {
        throw new UnsupportedOperationException("Player is offline");
    }

    public boolean isOp() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean isOnline() {
        return false;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public void setCompassTarget(Location loc) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public InetSocketAddress getAddress() {
        throw new UnsupportedOperationException("Player is offline");
    }

    public void kickPlayer(String message) {
        throw new UnsupportedOperationException("Player is offline");
    }

    public void chat(String msg) {
        throw new UnsupportedOperationException("Player is offline");
    }

    public boolean performCommand(String command) {
        throw new UnsupportedOperationException("Player is offline");
    }

    public boolean isSneaking() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setSneaking(boolean sneak) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void updateInventory() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Location getEyeLocation() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void damage(int amount) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void damage(int amount, Entity source) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setVelocity(Vector velocity) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Vector getVelocity() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public int getMaximumNoDamageTicks() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setMaximumNoDamageTicks(int ticks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public int getLastDamage() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setLastDamage(int damage) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public int getNoDamageTicks() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setNoDamageTicks(int ticks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean teleport(Location location) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean teleport(Entity destination) {
        return teleport(destination.getLocation());
    }

    public Entity getPassenger() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean setPassenger(Entity passenger) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean eject() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public Location getCompassTarget() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void sendRawMessage(String message) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean isSleeping() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public int getSleepTicks() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public List<Entity> getNearbyEntities(double x, double y, double z) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean isDead() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public float getFallDistance() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setFallDistance(float distance) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void saveData() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void loadData() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void setSleepingIgnored(boolean isSleeping) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public boolean isSleepingIgnored() {
        return false;
    }

    public void awardAchievement(Achievement achievement) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void incrementStatistic(Statistic statistic) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void incrementStatistic(Statistic statistic, int amount) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void incrementStatistic(Statistic statistic, Material material) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
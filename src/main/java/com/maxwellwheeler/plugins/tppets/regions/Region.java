package com.maxwellwheeler.plugins.tppets.regions;

import com.maxwellwheeler.plugins.tppets.TPPets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Abstract class for {@link ProtectedRegion} and {@link LostAndFoundRegion}
 * @author GatheringExp
 *
 */
public abstract class Region {
    protected String regionName;
    protected String worldName;
    protected World world;
    protected Location minLoc;
    protected Location maxLoc;

    /**
     * A general constructor, used primarily for regeneration of the regions from databases.
     * @param regionName The name of the zone.
     * @param worldName The name of the world the zone is in. If worldName points to a non-existent world, world will be null but worldName will be what the world was.
     * @param minX The Region is generated based on two points: the minimum and maximum of the cube. This is the minimum point's X location.
     * @param minY The Region is generated based on two points: the minimum and maximum of the cube. This is the minimum point's Y location.
     * @param minZ The Region is generated based on two points: the minimum and maximum of the cube. This is the minimum point's Z location.
     * @param maxX The Region is generated based on two points: the minimum and maximum of the cube. This is the maximum point's X location.
     * @param maxY The Region is generated based on two points: the minimum and maximum of the cube. This is the maximum point's Y location.
     * @param maxZ The Region is generated based on two points: the minimum and maximum of the cube. This is the maximum point's Z location.
     */
    Region(String regionName, String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(regionName, worldName, Bukkit.getServer().getWorld(worldName), new Location(Bukkit.getServer().getWorld(worldName), minX, minY, minZ), new Location(Bukkit.getServer().getWorld(worldName), maxX, maxY, maxZ));
    }
    
    /**
     * The same style of constructor as Region(String zoneName, String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ), but with the coordinates represented by Location objects that include the world.
     * @param regionName The name of the zone.
     * @param worldName The name of the world the zone is in. If worldName points to a non-existent world, world will be null but worldName will be what the world was.
     * @param minLoc The Region is generated based on two points: the minimum and maximum of the cube. This is the minimum point.
     * @param maxLoc The Region is generated based on two points: the minimum and maximum of the cube. This is the maximum point.
     */
    public Region(String regionName, String worldName, Location minLoc, Location maxLoc) {
        this(regionName, worldName, Bukkit.getServer().getWorld(worldName), minLoc, maxLoc);
    }
    
    /**
     * Same style of constructor as Region(String zoneName, String worldName, Location minLoc, Location maxLoc), but with the World reference explicitly made.
     * @param regionName The name of the zone.
     * @param worldName The name of the world the zone is in. If worldName points to a non-existent world, world will be null but worldName will be what the world was.
     * @param world An explicit reference to the world the region is in.
     * @param minLoc The Region is generated based on two points: the minimum and maximum of the cube. This is the minimum point.
     * @param maxLoc The Region is generated based on two points: the minimum and maximum of the cube. This is the maximum point.
     */
    public Region(String regionName, String worldName, World world, Location minLoc, Location maxLoc) {
        this.regionName = regionName;
        this.worldName = worldName;
        this.world = world;
        this.minLoc = minLoc;
        this.maxLoc = maxLoc;
    }
    
    /**
     * Local utility function to test if argument middle is between minimum and maximum. Includes the endpoints.
     * @param min The minimum value of comparison.
     * @param middle The number being evaluated.
     * @param max The maximum value of comparison.
     * @return True if middle is between or including max and min.
     */
    private boolean isBetween(int min, int middle, int max) {
        return (middle >= min && middle <= max);
    }

    /**
     * Checks if a location is within the region instance.
     * @param lc The location to check
     * @return True if location is within the region instance, false otherwise.
     */
    public boolean isInRegion(Location lc) {
        return this.world != null && isBetween(this.minLoc.getBlockX(), lc.getBlockX(), this.maxLoc.getBlockX()) && isBetween(this.minLoc.getBlockY(), lc.getBlockY(), this.maxLoc.getBlockY()) && isBetween(this.minLoc.getBlockZ(), lc.getBlockZ(), this.maxLoc.getBlockZ());
    }
    
    /**
     * Implementing regions need a way to communicate with the log file.
     */
    public abstract String toString();

    public String getRegionName() {
        return regionName;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getMinLoc() {
        return minLoc;
    }
    
    public Location getMaxLoc() {
        return maxLoc;
    }
}

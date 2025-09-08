package com.github.idimabr.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Cuboid {

    private World world;
    private Location corner1;
    private Location corner2;
    private Location sign;

    public Cuboid(Location corner1, Location corner2) {
        if (corner1 == null || corner2 == null) return;
        if (corner1.getWorld() == null || corner2.getWorld() == null) return;
        if (!corner1.getWorld().equals(corner2.getWorld())) throw new IllegalArgumentException("Corners must be in the same world.");

        this.world = corner1.getWorld();
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public Location getCenter() {
        double x = (corner1.getX() + corner2.getX()) / 2;
        double y = (corner1.getY() + corner2.getY()) / 2;
        double z = (corner1.getZ() + corner2.getZ()) / 2;
        return new Location(world, x, y, z);
    }

    public void clear(){
        for (int x = Math.min(corner1.getBlockX(), corner2.getBlockX()); x <= Math.max(corner1.getBlockX(), corner2.getBlockX()); x++) {
            for (int y = Math.min(corner1.getBlockY(), corner2.getBlockY()); y <= Math.max(corner1.getBlockY(), corner2.getBlockY()); y++) {
                for (int z = Math.min(corner1.getBlockZ(), corner2.getBlockZ()); z <= Math.max(corner1.getBlockZ(), corner2.getBlockZ()); z++) {
                    Location loc = new Location(world, x, y, z);
                    loc.getBlock().setType(org.bukkit.Material.AIR);
                }
            }
        }
    }

    public List<Block> getBlocks(){
        List<Block> blocks = new ArrayList<>();
        for (int x = Math.min(corner1.getBlockX(), corner2.getBlockX()); x <= Math.max(corner1.getBlockX(), corner2.getBlockX()); x++) {
            for (int y = Math.min(corner1.getBlockY(), corner2.getBlockY()); y <= Math.max(corner1.getBlockY(), corner2.getBlockY()); y++) {
                for (int z = Math.min(corner1.getBlockZ(), corner2.getBlockZ()); z <= Math.max(corner1.getBlockZ(), corner2.getBlockZ()); z++) {
                    Location loc = new Location(world, x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }
        return blocks;
    }

    public boolean contains(Location location) {
        if (location == null || !location.getWorld().equals(world)) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= Math.min(corner1.getX(), corner2.getX()) &&
                x <= Math.max(corner1.getX(), corner2.getX()) &&
                y >= Math.min(corner1.getY(), corner2.getY()) &&
                y <= Math.max(corner1.getY(), corner2.getY()) &&
                z >= Math.min(corner1.getZ(), corner2.getZ()) &&
                z <= Math.max(corner1.getZ(), corner2.getZ());
    }
}

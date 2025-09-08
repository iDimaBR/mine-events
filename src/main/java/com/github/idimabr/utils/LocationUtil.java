package com.github.idimabr.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {

    public static String serialize(Location loc) {
        return loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName();
    }
 
    public static Location deserialize(String s) {
        try {
            if (s == null) return null;
            if (!s.contains(";")) return null;
            final String[] parts = s.split(";");
            if (parts.length < 3) return null;

            final double x = Double.parseDouble(parts[0]);
            final double y = Double.parseDouble(parts[1]);
            final double z = Double.parseDouble(parts[2]);
            final String u = parts[3];
            final World w = Bukkit.getServer().getWorld(u);
            return new Location(w, x, y, z);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
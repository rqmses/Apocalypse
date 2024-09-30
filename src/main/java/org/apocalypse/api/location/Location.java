package org.apocalypse.api.location;

import org.apocalypse.api.utils.LocationUtils;
import org.bukkit.World;

public class Location {

    private final org.bukkit.Location location;

    public Location(org.bukkit.Location location) {
        this.location = location;
    }

    public Location(int x, int z) {
        this.location = new org.bukkit.Location(LocationUtils.WORLD, x, 0, z);
    }

    public Location(int x, int y, int z) {
        this.location = new org.bukkit.Location(LocationUtils.WORLD, x, y, z);
    }

    public Location(int x, int y, int z, int yaw) {
        this.location = new org.bukkit.Location(LocationUtils.WORLD, x, y, z, yaw, 0);
    }

    public Location(int x, int y, int z, int yaw, int pitch) {
        this.location = new org.bukkit.Location(LocationUtils.WORLD, x, y, z, yaw, pitch);
    }

    public Location(String x, String y, String z) {
        this.location = new org.bukkit.Location(LocationUtils.WORLD, parse(x), parse(y), parse(z));
    }

    public Location(String x, String y, String z, String yaw) {
        this.location = new org.bukkit.Location(LocationUtils.WORLD, parse(x), parse(y), parse(z), parse(yaw), 0);
    }

    public org.bukkit.Location get() {
        return LocationUtils.toAverage(this.location);
    }

    public org.bukkit.Location get(World world) {
        org.bukkit.Location loc = this.location.clone();
        loc.setWorld(world);
        return LocationUtils.toAverage(loc);
    }

    public void setWorld(World world) {
        this.location.setWorld(world);
    }

    public World getWorld() {
        return this.location.getWorld();
    }

    private int parse(String string) {
        return Integer.parseInt(string);
    }

    public int getX() {
        return this.location.getBlockX();
    }

    public int getY() {
        return this.location.getBlockY();
    }

    public int getZ() {
        return this.location.getBlockZ();
    }

    public double distance(Location location) {
        return this.get(location.getWorld()).distance(location.get());
    }
}

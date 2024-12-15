package io.github.pokemeetup.world.model;


public class TileType {
    public static final int GRASS = 1;
    public static final int DIRT = 2;
    public static final int SAND = 10;
    public static final int DESERT_SAND = 11;

    public static boolean isPassable(int tileId) {
        
        return true;
    }
}

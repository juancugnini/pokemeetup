package io.github.pokemeetup.world.model;

import java.util.ArrayList;
import java.util.List;

public class ChunkData {
    private int[][] tiles;
    private List<WorldObject> objects = new ArrayList<>();

    public int[][] getTiles() {
        return tiles;
    }

    public void setTiles(int[][] tiles) {
        this.tiles = tiles;
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    public void setObjects(List<WorldObject> objects) {
        this.objects = objects;
    }
}

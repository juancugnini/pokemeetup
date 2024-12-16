package io.github.pokemeetup.world.service;

import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.WorldObject;

import java.util.List;

public interface WorldObjectManager {
    void generateObjectsForChunk(int chunkX, int chunkY, int[][] tiles, Biome biome);
    void addObject(WorldObject object);
    void removeObject(String objectId);
    List<WorldObject> getObjectsForChunk(int chunkX, int chunkY);
}

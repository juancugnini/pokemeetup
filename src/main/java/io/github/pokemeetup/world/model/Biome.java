package io.github.pokemeetup.world.model;

import java.util.List;
import java.util.Map;

public class Biome {
    private BiomeType type;
    private List<Integer> allowedTileTypes;
    private Map<Integer,Double> tileDistribution;
    private List<String> spawnableObjects;
    private Map<String,Double> spawnChances;

    public Biome(BiomeType type,
                 List<Integer> allowedTileTypes,
                 Map<Integer,Double> tileDistribution,
                 List<String> spawnableObjects,
                 Map<String,Double> spawnChances) {
        this.type = type;
        this.allowedTileTypes = allowedTileTypes;
        this.tileDistribution = tileDistribution;
        this.spawnableObjects = spawnableObjects;
        this.spawnChances = spawnChances;
    }

    public BiomeType getType() {
        return type;
    }

    public List<Integer> getAllowedTileTypes() {
        return allowedTileTypes;
    }

    public Map<Integer, Double> getTileDistribution() {
        return tileDistribution;
    }

    public List<String> getSpawnableObjects() {
        return spawnableObjects;
    }

    // Now referencing ObjectType directly
    public double getSpawnChanceForObject(ObjectType objType) {
        return spawnChances.getOrDefault(objType.name(), 0.0);
    }
}

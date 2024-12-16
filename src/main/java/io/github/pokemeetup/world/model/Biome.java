package io.github.pokemeetup.world.model;

import lombok.Getter;

import java.util.List;
import java.util.Map;

public class Biome {
    @Getter
    private BiomeType type;
    @Getter
    private List<Integer> allowedTileTypes;
    @Getter
    private Map<Integer,Double> tileDistribution;
    @Getter
    private List<String> spawnableObjects;
    private final Map<String,Double> spawnChances;

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

    public double getSpawnChanceForObject(ObjectType objType) {
        return spawnChances.getOrDefault(objType.name(), 0.0);
    }
}

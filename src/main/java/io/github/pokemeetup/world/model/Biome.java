package io.github.pokemeetup.world.model;

import java.util.Map;

public class Biome {
    private BiomeType type;
    private Map<Integer,Integer> tileDistribution;

    public Biome(BiomeType type, Map<Integer,Integer> tileDistribution) {
        this.type = type;
        this.tileDistribution = tileDistribution;
    }

    public BiomeType getType() {
        return type;
    }

    public Map<Integer, Integer> getTileDistribution() {
        return tileDistribution;
    }
}

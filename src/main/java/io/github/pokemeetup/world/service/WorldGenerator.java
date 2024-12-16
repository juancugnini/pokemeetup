package io.github.pokemeetup.world.service;

import com.badlogic.gdx.math.MathUtils;
import io.github.pokemeetup.utils.OpenSimplex2;
import io.github.pokemeetup.world.config.WorldConfig;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.BiomeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorldGenerator {

    private final WorldConfig config;
    private final WorldObjectManager worldObjectManager;
    private Map<BiomeType, Biome> biomes;

    private static final float NOISE_SCALE = 0.005f;

    @Autowired
    public WorldGenerator(WorldConfig config, WorldObjectManager worldObjectManager) {
        this.config = config;
        this.worldObjectManager = worldObjectManager;
    }

    public void setBiomes(Map<BiomeType, Biome> biomes) {
        this.biomes = biomes;
    }

    public int[][] generateChunk(int chunkX, int chunkY) {
        if (biomes == null || biomes.isEmpty()) {
            int chunkSize = config.getChunkSize();
            return new int[chunkSize][chunkSize];
        }

        float n = OpenSimplex2.noise2(config.getSeed(), chunkX * NOISE_SCALE, chunkY * NOISE_SCALE);
        BiomeType selectedBiome = n > 0 ? BiomeType.PLAINS : BiomeType.DESERT;
        Biome biome = biomes.get(selectedBiome);

        int chunkSize = config.getChunkSize();
        int[][] tiles = new int[chunkSize][chunkSize];
        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                tiles[x][y] = pickTileFromDistribution(biome);
            }
        }

        worldObjectManager.generateObjectsForChunk(chunkX, chunkY, tiles, biome);
        return tiles;
    }

    private int pickTileFromDistribution(Biome biome) {
        Map<Integer, Double> distribution = biome.getTileDistribution();
        double total = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = MathUtils.random((float) total);
        double cumulative = 0;
        for (Map.Entry<Integer, Double> entry : distribution.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return distribution.keySet().iterator().next();
    }
}

package io.github.pokemeetup.world.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.BiomeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BiomeConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(BiomeConfigurationLoader.class);

    public static class BiomeDefinition {
        public String name;
        public String type;
        public ArrayList<Integer> allowedTileTypes;
        public HashMap<String,Double> tileDistribution;
        public ArrayList<String> spawnableObjects;
        public HashMap<String,Double> spawnChances;

        public BiomeDefinition() {
            allowedTileTypes = new ArrayList<>();
            tileDistribution = new HashMap<>();
            spawnableObjects = new ArrayList<>();
            spawnChances = new HashMap<>();
        }
    }

    public static class BiomeRoot {
        public ArrayList<BiomeDefinition> biomes = new ArrayList<>();
    }

    public Map<BiomeType, Biome> loadBiomes(String configFilePath) {
        FileHandle file = Gdx.files.internal(configFilePath);
        if (!file.exists()) {
            logger.error("Biome config file not found: {}", configFilePath);
            return Collections.emptyMap();
        }

        Json json = new Json();
        json.setIgnoreUnknownFields(true);

        BiomeRoot root = json.fromJson(BiomeRoot.class, file.readString());
        if (root == null || root.biomes.isEmpty()) {
            logger.warn("No biomes found in file: {}", configFilePath);
            return Collections.emptyMap();
        }

        Map<BiomeType,Biome> biomeMap = new HashMap<>();
        for (BiomeDefinition def : root.biomes) {
            BiomeType type = BiomeType.valueOf(def.type);

            Map<Integer, Double> distribution = new HashMap<>();
            for (Map.Entry<String, Double> entry : def.tileDistribution.entrySet()) {
                distribution.put(Integer.valueOf(entry.getKey()), entry.getValue());
            }

            Biome biome = new Biome(
                    type,
                    def.allowedTileTypes,
                    distribution,
                    def.spawnableObjects,
                    def.spawnChances
            );

            biomeMap.put(type, biome);
        }

        logger.info("Loaded {} biomes from {}", biomeMap.size(), configFilePath);
        return biomeMap;
    }
}

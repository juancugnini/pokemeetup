package io.github.pokemeetup.world.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.BiomeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class BiomeConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(BiomeConfigurationLoader.class);

    public Map<BiomeType, Biome> loadBiomes(String configFilePath) {
        FileHandle file = Gdx.files.internal(configFilePath);
        if(!file.exists()) {
            logger.error("Biome config file not found: {}", configFilePath);
            return Collections.emptyMap();
        }

        Json json = new Json();
        BiomeConfig config = json.fromJson(BiomeConfig.class, file.readString());

        Map<BiomeType,Biome> biomeMap = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Integer>> entry: config.getBiomes().entrySet()) {
            BiomeType type = BiomeType.valueOf(entry.getKey());
            Map<Integer, Integer> distribution = entry.getValue().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> Integer.valueOf(e.getKey()),
                            e -> (int)Math.round(((Number)e.getValue()).doubleValue())
                    ));

            Biome biome = new Biome(type, distribution);
            biomeMap.put(type, biome);
        }

        return biomeMap;
    }
}

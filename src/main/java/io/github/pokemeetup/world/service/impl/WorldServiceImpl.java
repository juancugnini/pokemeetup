package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.world.config.BiomeConfigurationLoader;
import io.github.pokemeetup.world.config.WorldConfig;
import io.github.pokemeetup.world.model.WorldData;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.BiomeType;
import io.github.pokemeetup.world.service.WorldGenerator;
import io.github.pokemeetup.world.service.WorldService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.Map;

@Service
public class WorldServiceImpl implements WorldService {
    private static final Logger logger = LoggerFactory.getLogger(WorldServiceImpl.class);

    @Value("${world.seed:12345}")
    private long seed;

    @Value("${world.biomeConfigPath:config/biomes.json}")
    private String biomeConfigPath;

    @Value("${world.saveFilePath:save/worldData.json}")
    private String saveFilePath;

    private WorldConfig worldConfig;
    private WorldData worldData;
    private WorldGenerator generator;

    @PostConstruct
    public void init() {
        this.worldConfig = new WorldConfig(seed);
        initialize();
    }

    @Override
    public void initialize() {
        BiomeConfigurationLoader loader = new BiomeConfigurationLoader();
        Map<BiomeType,Biome> biomes = loader.loadBiomes(biomeConfigPath);

        this.generator = new WorldGenerator(worldConfig, biomes);
        loadWorldData();
        if (this.worldData == null) {
            this.worldData = new WorldData(worldConfig.getSeed());
            logger.info("Created new world data with seed: {}", worldConfig.getSeed());
        }
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public void saveWorldData() {
        if (worldData == null) return;
        Json json = new Json();
        String dataStr = json.prettyPrint(worldData);
        FileHandle file = Gdx.files.local(saveFilePath);
        file.writeString(dataStr, false);
        logger.info("Saved world data to {}", saveFilePath);
    }

    @Override
    public void loadWorldData() {
        FileHandle file = Gdx.files.local(saveFilePath);
        if (!file.exists()) {
            logger.info("No existing world data file found at {}", saveFilePath);
            return;
        }

        Json json = new Json();
        this.worldData = json.fromJson(WorldData.class, file.readString());
        logger.info("Loaded world data from {}", saveFilePath);
    }

    @Override
    public int[][] getChunkTiles(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        if (!worldData.getChunks().containsKey(key)) {
            int[][] tiles = generator.generateChunk(chunkX, chunkY);
            worldData.getChunks().put(key, tiles);
            return tiles;
        }
        return worldData.getChunks().get(key);
    }

    @Override
    public void setPlayerData(PlayerData playerData) {
        worldData.getPlayers().put(playerData.getUsername(), playerData);
    }

    @Override
    public PlayerData getPlayerData(String username) {
        return worldData.getPlayers().get(username);
    }
}

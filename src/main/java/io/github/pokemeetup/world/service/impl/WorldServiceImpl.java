package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.world.config.BiomeConfigurationLoader;
import io.github.pokemeetup.world.config.WorldConfig;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.BiomeType;
import io.github.pokemeetup.world.model.WorldData;
import io.github.pokemeetup.world.service.WorldGenerator;
import io.github.pokemeetup.world.service.WorldObjectManager;
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

    @Value("${world.saveFilePath:save/worldData.json}")
    private String saveFilePath;

    private final WorldConfig worldConfig;
    private final WorldData worldData = new WorldData();
    private final WorldGenerator generator;

    public WorldServiceImpl(WorldConfig worldConfig, WorldGenerator generator) {
        this.worldConfig = worldConfig;
        this.generator = generator;
    }

    @PostConstruct
    public void init() {
        initialize();
    }

    @Override
    public void initialize() {
        BiomeConfigurationLoader loader = new BiomeConfigurationLoader();
        Map<BiomeType, Biome> biomes = loader.loadBiomes("assets/config/biomes.json");
        generator.setBiomes(biomes);

        loadWorldData();
        if (this.worldData.getSeed() == 0) {
            // If seed not set, set now
            worldData.setSeed(worldConfig.getSeed());
            logger.info("Created new world data with seed: {}", worldConfig.getSeed());
        }
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public void saveWorldData() {
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
        WorldData loadedData = json.fromJson(WorldData.class, file.readString());
        // Merge loaded data into our worldData
        worldData.setSeed(loadedData.getSeed());
        worldData.setPlayers(loadedData.getPlayers());
        worldData.setChunks(loadedData.getChunks());
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

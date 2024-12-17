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
import io.github.pokemeetup.world.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WorldServiceImpl implements WorldService {
    private static final Logger logger = LoggerFactory.getLogger(WorldServiceImpl.class);
    private final WorldConfig worldConfig;
    private final WorldData worldData = new WorldData();
    private final WorldGenerator generator;

    @Value("${world.saveDir:save/worlds/}")
    private String saveDir;

    @Value("${world.defaultSave:save/worldData.json}")
    private String defaultSaveFile;

    private boolean initialized = false;

    public WorldServiceImpl(WorldConfig worldConfig, WorldGenerator generator) {
        this.worldConfig = worldConfig;
        this.generator = generator;
    }

    @Override
    public void initIfNeeded() {
        if (!initialized) {
            BiomeConfigurationLoader loader = new BiomeConfigurationLoader();
            Map<BiomeType, Biome> biomes = loader.loadBiomes("assets/config/biomes.json"); // uses Gdx now
            generator.setBiomes(biomes);

            loadWorldData();
            if (worldData.getSeed() == 0) {
                worldData.setSeed(worldConfig.getSeed());
            }
            initialized = true;
        }
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public void saveWorldData() {
        FileHandle dir = Gdx.files.local(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Created directories for save data at {}", dir.path());
        }

        String targetPath;
        if (worldData.getWorldName() != null && !worldData.getWorldName().isEmpty()) {
            targetPath = saveDir + worldData.getWorldName() + ".json";
        } else {
            targetPath = defaultSaveFile;
        }

        Json json = new Json();
        String dataStr = json.prettyPrint(worldData);
        FileHandle file = Gdx.files.local(targetPath);
        file.writeString(dataStr, false);
        logger.info("Saved world data to {}", file.path());
    }

    @Override
    public boolean createWorld(String worldName, long seed) {
        FileHandle dir = Gdx.files.local(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
            logger.info("Created directories for save data at {}", dir.path());
        }

        FileHandle file = Gdx.files.local(saveDir + worldName + ".json");
        if (file.exists()) {
            logger.warn("World '{}' already exists, cannot create", worldName);
            return false;
        }

        WorldData newWorld = new WorldData();
        newWorld.setWorldName(worldName);
        newWorld.setSeed(seed);

        Json json = new Json();
        String dataStr = json.prettyPrint(newWorld);
        file.writeString(dataStr, false);
        logger.info("Created and saved new world '{}' with seed {} at {}", worldName, seed, file.path());

        return true;
    }


    @Override
    public void loadWorldData() {
        FileHandle file = Gdx.files.local(defaultSaveFile);
        if (!file.exists()) {
            logger.info("No default world data file found at {}", defaultSaveFile);
            return;
        }

        Json json = new Json();
        WorldData loadedData = json.fromJson(WorldData.class, file.readString());
        mergeLoadedData(loadedData);
        logger.info("Loaded world data from {}", defaultSaveFile);
    }

    private void mergeLoadedData(WorldData loadedData) {
        worldData.setWorldName(loadedData.getWorldName());
        worldData.setSeed(loadedData.getSeed());
        worldData.setPlayers(loadedData.getPlayers());
        worldData.setChunks(loadedData.getChunks());
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

    @Override
    public List<String> getAvailableWorlds() {
        List<String> worlds = new ArrayList<>();
        FileHandle dir = Gdx.files.local(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileHandle[] files = dir.list(fileHandle -> fileHandle.getName().endsWith(".json"));
        if (files != null) {
            for (FileHandle f : files) {
                String fileName = f.nameWithoutExtension();
                worlds.add(fileName);
            }
        }
        return worlds;
    }

    @Override
    public void loadWorld(String worldName) {
        FileHandle file = Gdx.files.local(saveDir + worldName + ".json");
        if (!file.exists()) {
            logger.warn("World file {} does not exist, cannot load", file.path());
            return;
        }

        Json json = new Json();
        WorldData loadedData = json.fromJson(WorldData.class, file.readString());
        mergeLoadedData(loadedData);
        logger.info("Loaded world data for world: {} from {}", worldName, file.path());
    }

    @Override
    public void deleteWorld(String worldName) {
        FileHandle file = Gdx.files.local(saveDir + worldName + ".json");
        if (!file.exists()) {
            logger.warn("World file {} does not exist, cannot delete", file.path());
            return;
        }

        file.delete();
        logger.info("Deleted world file: {}", file.path());

        // If the currently loaded world is the one deleted, we might clear it out or re-init
        if (worldData.getWorldName() != null && worldData.getWorldName().equals(worldName)) {
            // Clear current loaded world data
            worldData.setWorldName(null);
            worldData.setSeed(0);
            worldData.getPlayers().clear();
            worldData.getChunks().clear();
            logger.info("Cleared current loaded world data because it was deleted.");
        }
    }

}

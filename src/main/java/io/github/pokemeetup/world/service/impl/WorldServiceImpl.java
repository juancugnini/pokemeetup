package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Json;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.world.config.BiomeConfigurationLoader;
import io.github.pokemeetup.world.config.WorldConfig;
import io.github.pokemeetup.world.model.*;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldGenerator;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class WorldServiceImpl implements WorldService {
    private static final Logger logger = LoggerFactory.getLogger(WorldServiceImpl.class);

    private final WorldConfig worldConfig;
    private final WorldGenerator worldGenerator;
    private final WorldObjectManager worldObjectManager;
    private final TileManager tileManager;

    @Value("${world.saveDir:assets/save/worlds/}")
    private String saveDir;

    @Value("${world.defaultSave:assets/save/worldData.json}")
    private String defaultSaveFile;

    private final WorldData worldData = new WorldData();
    private boolean initialized = false;

    public WorldServiceImpl(WorldConfig worldConfig,
                            WorldGenerator worldGenerator,
                            WorldObjectManager worldObjectManager,
                            TileManager tileManager) {
        this.worldConfig = worldConfig;
        this.worldGenerator = worldGenerator;
        this.worldObjectManager = worldObjectManager;
        this.tileManager = tileManager;
    }

    @Override
    public void initIfNeeded() {
        if (!initialized) {
            BiomeConfigurationLoader loader = new BiomeConfigurationLoader();
            Map<BiomeType, Biome> biomes = loader.loadBiomes("assets/config/biomes.json");

            if (worldData.getSeed() == 0) {
                worldData.setSeed(worldConfig.getSeed());
            }

            worldGenerator.setSeedAndBiomes(worldData.getSeed(), biomes);
            worldObjectManager.initialize();
            tileManager.initIfNeeded();

            loadWorldData();
            initialized = true;
            logger.info("WorldService initialized with seed {}", worldData.getSeed());
        }
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public void generateWorldThumbnail(String worldName) {
        initIfNeeded();

        int previewSize = 8;
        int tileSize = 32;
        int iconWidth = 128;
        int iconHeight = 128;

        float scaleX = (float)iconWidth / (previewSize * tileSize);
        float scaleY = (float)iconHeight / (previewSize * tileSize);
        float scale = Math.min(scaleX, scaleY);

        // Create an off-screen framebuffer
        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, iconWidth, iconHeight, false);

        // A SpriteBatch for off-screen rendering
        SpriteBatch batch = new SpriteBatch();

        // Begin rendering to the off-screen framebuffer
        fbo.begin();
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set up a camera with top-down coordinates (y-down)
        OrthographicCamera camera = new OrthographicCamera(iconWidth, iconHeight);
        camera.setToOrtho(true, iconWidth, iconHeight);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Set up a transform matrix for scaling and positioning
        com.badlogic.gdx.math.Matrix4 transform = batch.getTransformMatrix();
        transform.idt();

        // Calculate world area and center it
        float worldWidth = previewSize * tileSize;
        float worldHeight = previewSize * tileSize;
        transform.translate(iconWidth / 2f, iconHeight / 2f, 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-worldWidth / 2f, -worldHeight / 2f, 0);

        batch.setTransformMatrix(transform);

        int centerX = 0;
        int centerY = 0;

        // Render tiles
        for (int dy = 0; dy < previewSize; dy++) {
            for (int dx = 0; dx < previewSize; dx++) {
                int tileX = centerX + dx - previewSize / 2;
                int tileY = centerY + dy - previewSize / 2;
                int chunkX = tileX / 16;
                int chunkY = tileY / 16;
                int[][] tiles = getChunkTiles(chunkX, chunkY);
                if (tiles != null) {
                    int localX = Math.floorMod(tileX, 16);
                    int localY = Math.floorMod(tileY, 16);
                    if (localX >= 0 && localX < 16 && localY >= 0 && localY < 16) {
                        int tileType = tiles[localX][localY];
                        TextureRegion region = tileManager.getRegionForTile(tileType);
                        if (region != null) {
                            float worldPixelX = dx * tileSize;
                            float worldPixelY = dy * tileSize;
                            batch.draw(region, worldPixelX, worldPixelY, tileSize, tileSize);
                        }
                    }
                }
            }
        }

        // Render world objects
        Set<String> processedChunks = new HashSet<>();
        for (int dy = 0; dy < previewSize; dy++) {
            for (int dx = 0; dx < previewSize; dx++) {
                int tileX = centerX + dx - previewSize / 2;
                int tileY = centerY + dy - previewSize / 2;
                int chunkX = tileX / 16;
                int chunkY = tileY / 16;
                String key = chunkX + "," + chunkY;
                if (!processedChunks.contains(key)) {
                    List<WorldObject> objs = worldObjectManager.getObjectsForChunk(chunkX, chunkY);
                    for (WorldObject obj : objs) {
                        int objTileX = obj.getTileX();
                        int objTileY = obj.getTileY();
                        if (objTileX >= centerX - previewSize / 2 && objTileX < centerX + previewSize / 2 &&
                                objTileY >= centerY - previewSize / 2 && objTileY < centerY + previewSize / 2) {
                            float worldPixelX = (objTileX - (centerX - previewSize / 2)) * tileSize;
                            float worldPixelY = (objTileY - (centerY - previewSize / 2)) * tileSize;
                            if (obj.getTexture() != null) {
                                batch.draw(obj.getTexture(), worldPixelX, worldPixelY);
                            }
                        }
                    }
                    processedChunks.add(key);
                }
            }
        }

        batch.end();

        // Capture the pixels from the off-screen framebuffer BEFORE calling fbo.end()
        Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, iconWidth, iconHeight);

        // Now end the fbo
        fbo.end();

        // Save the captured pixmap as a PNG
        FileHandle iconFile = Gdx.files.local(saveDir + worldName + "/icon.png");
        PixmapIO.writePNG(iconFile, pm);

        pm.dispose();
        batch.dispose();
        fbo.dispose();

        logger.info("Generated world thumbnail for '{}'", worldName);
    }


    @Override
    public boolean createWorld(String worldName, long seed) {
        FileHandle worldDir = Gdx.files.local(saveDir + worldName + "/");
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        }

        FileHandle worldFile = Gdx.files.local(worldDir.path() + "/" + worldName + ".json");
        if (worldFile.exists()) {
            logger.warn("World '{}' already exists, cannot create", worldName);
            return false;
        }

        WorldData newWorld = new WorldData();
        newWorld.setWorldName(worldName);
        newWorld.setSeed(seed);
        long now = System.currentTimeMillis();
        newWorld.setCreatedDate(now);
        newWorld.setLastPlayed(now);
        newWorld.setPlayedTime(0);

        Json json = new Json();
        String dataStr = json.prettyPrint(newWorld);
        worldFile.writeString(dataStr, false);
        logger.info("Created and saved new world '{}' with seed {} at {}", worldName, seed, worldFile.path());

        try {
            generatePlaceholderWorldIcon(worldName);
        } catch (IOException e) {
            logger.warn("Failed to generate placeholder icon for world {}: {}", worldName, e.getMessage());
        }

        return true;
    }

    private void generatePlaceholderWorldIcon(String worldName) throws IOException {
        FileHandle src = Gdx.files.internal("assets/icons/default_world_icon.png");
        FileHandle destDir = Gdx.files.local(saveDir + worldName + "/");
        if (!destDir.exists()) destDir.mkdirs();
        FileHandle dest = Gdx.files.local(saveDir + worldName + "/icon.png");
        src.copyTo(dest);
        logger.info("Generated placeholder world icon for '{}'", worldName);
    }

    @Override
    public void saveWorldData() {
        if (worldData.getWorldName() == null || worldData.getWorldName().isEmpty()) {
            FileHandle file = Gdx.files.local(defaultSaveFile);
            Json json = new Json();
            file.writeString(json.prettyPrint(worldData), false);
            logger.info("Saved default world data to {}", file.path());
            return;
        }

        String worldName = worldData.getWorldName();
        FileHandle worldDir = Gdx.files.local(saveDir + worldName + "/");
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        }

        FileHandle playersDir = Gdx.files.local(worldDir.path() + "/players/");
        if (!playersDir.exists()) playersDir.mkdirs();

        FileHandle chunksDir = Gdx.files.local(worldDir.path() + "/chunks/");
        if (!chunksDir.exists()) chunksDir.mkdirs();

        Json json = new Json();

        WorldData shallowData = new WorldData();
        shallowData.setWorldName(worldData.getWorldName());
        shallowData.setSeed(worldData.getSeed());
        shallowData.setCreatedDate(worldData.getCreatedDate());
        shallowData.setLastPlayed(worldData.getLastPlayed());
        shallowData.setPlayedTime(worldData.getPlayedTime());

        FileHandle worldFile = Gdx.files.local(worldDir.path() + "/" + worldName + ".json");
        worldFile.writeString(json.prettyPrint(shallowData), false);

        // Save each player
        for (Map.Entry<String, PlayerData> entry : worldData.getPlayers().entrySet()) {
            String username = entry.getKey();
            PlayerData pData = entry.getValue();
            FileHandle playerFile = Gdx.files.local(playersDir.path() + "/" + username + ".json");
            playerFile.writeString(json.prettyPrint(pData), false);
        }

        // Save each chunk
        for (Map.Entry<String, ChunkData> chunkEntry : worldData.getChunks().entrySet()) {
            String chunkKey = chunkEntry.getKey();
            ChunkData cData = chunkEntry.getValue();

            FileHandle chunkFile = Gdx.files.local(chunksDir.path() + "/" + chunkKey + ".json");
            chunkFile.writeString(json.prettyPrint(cData), false);
        }

        logger.info("Saved world data for '{}' to {}", worldName, worldDir.path());
    }

    @Override
    public void loadWorld(String worldName) {
        FileHandle worldDir = Gdx.files.local(saveDir + worldName + "/");
        if (!worldDir.exists()) {
            logger.warn("World directory {} does not exist", worldDir.path());
            return;
        }

        FileHandle worldFile = Gdx.files.local(worldDir.path() + "/" + worldName + ".json");
        if (!worldFile.exists()) {
            logger.warn("World file {} does not exist, cannot load", worldFile.path());
            return;
        }

        Json json = new Json();
        WorldData loadedData = json.fromJson(WorldData.class, worldFile.readString());
        worldData.setWorldName(loadedData.getWorldName());
        worldData.setSeed(loadedData.getSeed());
        worldData.setCreatedDate(loadedData.getCreatedDate());
        worldData.setLastPlayed(System.currentTimeMillis());
        worldData.setPlayedTime(loadedData.getPlayedTime());

        initIfNeeded();

        FileHandle playersDir = Gdx.files.local(worldDir.path() + "/players/");
        if (playersDir.exists()) {
            FileHandle[] playerFiles = playersDir.list(file -> file.getName().toLowerCase().endsWith(".json"));
            if (playerFiles != null) {
                for (FileHandle pFile : playerFiles) {
                    PlayerData pData = json.fromJson(PlayerData.class, pFile.readString());
                    worldData.getPlayers().put(pData.getUsername(), pData);
                }
            }
        }

        logger.info("Loaded world data for world: {} from {}", worldName, worldFile.path());
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
        logger.info("Loaded default world data from {}", defaultSaveFile);
    }

    private void mergeLoadedData(WorldData loadedData) {
        worldData.setWorldName(loadedData.getWorldName());
        worldData.setSeed(loadedData.getSeed());
        worldData.setCreatedDate(loadedData.getCreatedDate());
        worldData.setLastPlayed(loadedData.getLastPlayed());
        worldData.setPlayedTime(loadedData.getPlayedTime());
    }

    @Override
    public int[][] getChunkTiles(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        if (!worldData.getChunks().containsKey(key)) {
            loadOrGenerateChunk(chunkX, chunkY);
        }
        return worldData.getChunks().get(key).getTiles();
    }

    private void loadOrGenerateChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        String worldName = worldData.getWorldName();
        Json json = new Json();

        if (worldName != null && !worldName.isEmpty()) {
            FileHandle chunkFile = Gdx.files.local(saveDir + worldName + "/chunks/" + key + ".json");
            if (chunkFile.exists()) {
                ChunkData cData = json.fromJson(ChunkData.class, chunkFile.readString());
                worldObjectManager.loadObjectsForChunk(chunkX, chunkY, cData.getObjects());
                worldData.getChunks().put(key, cData);
                return;
            }
        }

        int[][] tiles = worldGenerator.generateChunk(chunkX, chunkY);
        ChunkData cData = new ChunkData();
        cData.setTiles(tiles);

        Biome biome = ((WorldGeneratorImpl) worldGenerator).getBiomeForChunk(chunkX, chunkY);
        List<WorldObject> objs = worldObjectManager.generateObjectsForChunk(chunkX, chunkY, tiles, biome, worldData.getSeed());
        cData.setObjects(objs);

        worldData.getChunks().put(key, cData);

        if (worldName != null && !worldName.isEmpty()) {
            FileHandle worldDir = Gdx.files.local(saveDir + worldName + "/");
            if (!worldDir.exists()) worldDir.mkdirs();
            FileHandle chunksDir = Gdx.files.local(worldDir.path() + "/chunks/");
            if (!chunksDir.exists()) chunksDir.mkdirs();

            FileHandle chunkFile = Gdx.files.local(chunksDir.path() + "/" + key + ".json");
            chunkFile.writeString(json.prettyPrint(cData), false);
        }
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

        for (FileHandle f : dir.list()) {
            if (f.isDirectory()) {
                FileHandle worldFile = Gdx.files.local(f.path() + "/" + f.name() + ".json");
                if (worldFile.exists()) {
                    worlds.add(f.name());
                }
            }
        }
        return worlds;
    }

    @Override
    public void deleteWorld(String worldName) {
        FileHandle worldDir = Gdx.files.local(saveDir + worldName + "/");
        if (!worldDir.exists()) {
            logger.warn("World directory {} does not exist, cannot delete", worldDir.path());
            return;
        }

        for (FileHandle f : worldDir.list()) {
            f.deleteDirectory();
        }
        worldDir.deleteDirectory();

        if (worldData.getWorldName() != null && worldData.getWorldName().equals(worldName)) {
            worldData.setWorldName(null);
            worldData.setSeed(0);
            worldData.getPlayers().clear();
            worldData.getChunks().clear();
            worldData.setCreatedDate(0);
            worldData.setLastPlayed(0);
            worldData.setPlayedTime(0);
            logger.info("Cleared current loaded world data because it was deleted.");
        }
        logger.info("Deleted world directory: {}", worldDir.path());
    }

    @Override
    public void regenerateChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        worldData.getChunks().remove(key);
        loadOrGenerateChunk(chunkX, chunkY);
    }
}

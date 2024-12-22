package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.pokemeetup.multiplayer.model.WorldObjectUpdate;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.repository.PlayerDataRepository;
import io.github.pokemeetup.world.biome.config.BiomeConfigurationLoader;
import io.github.pokemeetup.world.biome.model.Biome;
import io.github.pokemeetup.world.biome.model.BiomeType;
import io.github.pokemeetup.world.config.WorldConfig;
import io.github.pokemeetup.world.model.*;
import io.github.pokemeetup.world.repository.ChunkRepository;
import io.github.pokemeetup.world.repository.WorldMetadataRepository;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldGenerator;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Primary
@Profile("server")
public class ServerWorldServiceImpl extends BaseWorldServiceImpl implements WorldService {
    private static final Logger logger = LoggerFactory.getLogger(ServerWorldServiceImpl.class);
    private static final int TILE_SIZE = 32;
    private static final int CHUNK_SIZE = 16;

    private final WorldGenerator worldGenerator;
    private final WorldObjectManager worldObjectManager;
    private final TileManager tileManager;
    private final BiomeConfigurationLoader biomeLoader;
    private final WorldMetadataRepository worldMetadataRepo;
    private final PlayerDataRepository playerDataRepository;
    private final ChunkRepository chunkRepository;

    private final WorldData worldData = new WorldData();
    private boolean initialized = false;

    @Value("${world.defaultName:defaultWorld}")
    private String defaultWorldName;

    // Since server typically doesn't render, we can store a dummy camera if needed:
    private OrthographicCamera camera = null;

    public ServerWorldServiceImpl(WorldGenerator worldGenerator,
                                  WorldObjectManager worldObjectManager,
                                  TileManager tileManager,
                                  BiomeConfigurationLoader biomeLoader,
                                  WorldMetadataRepository worldMetadataRepo,
                                  PlayerDataRepository playerDataRepository,
                                  ChunkRepository chunkRepository) {
        this.worldGenerator = worldGenerator;
        this.worldObjectManager = worldObjectManager;
        this.tileManager = tileManager;
        this.biomeLoader = biomeLoader;
        this.worldMetadataRepo = worldMetadataRepo;
        this.playerDataRepository = playerDataRepository;
        this.chunkRepository = chunkRepository;
    }

    @Override
    public void initIfNeeded() {
        if (!initialized && worldData.getSeed() != 0) {
            Map<BiomeType, Biome> biomes = biomeLoader.loadBiomes("config/biomes.json");
            worldGenerator.setSeedAndBiomes(worldData.getSeed(), biomes);
            worldObjectManager.initialize();
            tileManager.initIfNeeded();
            initialized = true;
            logger.info("WorldService (server) initialized with seed {}", worldData.getSeed());
        }
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public TileManager getTileManager() {
        return tileManager;
    }

    @Override
    public void loadWorldData() {
        Optional<WorldMetadata> optMeta = worldMetadataRepo.findById(defaultWorldName);
        if (optMeta.isEmpty()) {
            logger.warn("No default world '{}' found in Cassandra for server. Please create a world or specify another default world.", defaultWorldName);
            return;
        }

        WorldMetadata meta = optMeta.get();
        worldData.setWorldName(meta.getWorldName());
        worldData.setSeed(meta.getSeed());
        worldData.setCreatedDate(meta.getCreatedDate());
        worldData.setLastPlayed(System.currentTimeMillis());
        worldData.setPlayedTime(meta.getPlayedTime());

        initIfNeeded();

        playerDataRepository.findAll().forEach(pd -> {
            worldData.getPlayers().put(pd.getUsername(), pd);
        });

        logger.info("Loaded default world data for '{}' from Cassandra (server)", defaultWorldName);
    }

    @Override
    public boolean createWorld(String worldName, long seed) {
        if (worldMetadataRepo.findById(worldName).isPresent()) {
            logger.warn("World '{}' already exists, cannot create", worldName);
            return false;
        }

        WorldMetadata meta = new WorldMetadata();
        meta.setWorldName(worldName);
        meta.setSeed(seed);
        long now = System.currentTimeMillis();
        meta.setCreatedDate(now);
        meta.setLastPlayed(now);
        meta.setPlayedTime(0);
        worldMetadataRepo.save(meta);

        worldData.setWorldName(worldName);
        worldData.setSeed(seed);
        worldData.setCreatedDate(now);
        worldData.setLastPlayed(now);
        worldData.setPlayedTime(0);

        logger.info("Created new world '{}' with seed {} in Cassandra (server)", worldName, seed);
        return true;
    }

    @Override
    public void saveWorldData() {
        if (worldData.getWorldName() == null || worldData.getWorldName().isEmpty()) {
            logger.info("No world loaded on server, nothing to save.");
            return;
        }

        Optional<WorldMetadata> optionalMeta = worldMetadataRepo.findById(worldData.getWorldName());
        WorldMetadata meta = optionalMeta.orElse(new WorldMetadata());

        meta.setWorldName(worldData.getWorldName());
        meta.setSeed(worldData.getSeed());
        meta.setCreatedDate(worldData.getCreatedDate());
        meta.setLastPlayed(System.currentTimeMillis());
        meta.setPlayedTime(worldData.getPlayedTime());
        worldMetadataRepo.save(meta);


        for (PlayerData pd : worldData.getPlayers().values()) {
            playerDataRepository.save(pd);
        }

        for (Map.Entry<String, ChunkData> entry : worldData.getChunks().entrySet()) {
            String[] parts = entry.getKey().split(",");
            int cx = Integer.parseInt(parts[0]);
            int cy = Integer.parseInt(parts[1]);
            ChunkData cData = entry.getValue();
            cData.setKey(new ChunkData.ChunkKey(cx, cy));
            chunkRepository.save(cData);
        }

        logger.info("Saved world data for '{}' to Cassandra (server)", worldData.getWorldName());
    }

    @Override
    public void loadWorld(String worldName) {
        Optional<WorldMetadata> optMeta = worldMetadataRepo.findById(worldName);
        if (optMeta.isEmpty()) {
            logger.warn("World '{}' does not exist in Cassandra (server).", worldName);
            return;
        }

        WorldMetadata meta = optMeta.get();
        worldData.setWorldName(meta.getWorldName());
        worldData.setSeed(meta.getSeed());
        worldData.setCreatedDate(meta.getCreatedDate());
        worldData.setLastPlayed(System.currentTimeMillis());
        worldData.setPlayedTime(meta.getPlayedTime());

        initIfNeeded();

        playerDataRepository.findAll().forEach(pd -> {
            worldData.getPlayers().put(pd.getUsername(), pd);
        });

        logger.info("Loaded world data for '{}' from Cassandra (server)", worldName);
    }

    @Override
    public int[][] getChunkTiles(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        if (!worldData.getChunks().containsKey(key)) {
            loadOrGenerateChunk(chunkX, chunkY);
        }
        ChunkData cData = worldData.getChunks().get(key);
        return cData != null ? cData.getTiles() : null;
    }

    private void loadOrGenerateChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        Optional<ChunkData> opt = chunkRepository.findById(new ChunkData.ChunkKey(chunkX, chunkY));
        if (opt.isPresent()) {
            ChunkData cData = opt.get();

            if (cData.getObjects() != null) {
                cData.setObjects(new ArrayList<>(cData.getObjects()));
            }

            worldObjectManager.loadObjectsForChunk(chunkX, chunkY, cData.getObjects());
            worldData.getChunks().put(key, cData);
            return;
        }


        int[][] tiles = worldGenerator.generateChunk(chunkX, chunkY);
        ChunkData cData = new ChunkData();
        cData.setKey(new ChunkData.ChunkKey(chunkX, chunkY));
        cData.setTiles(tiles);

        Biome biome = worldGenerator.getBiomeForChunk(chunkX, chunkY);
        List<WorldObject> objs = worldObjectManager.generateObjectsForChunk(chunkX, chunkY, tiles, biome, worldData.getSeed());
        cData.setObjects(objs);
        worldData.getChunks().put(key, cData);
        chunkRepository.save(cData);
    }

    @Override
    public boolean isChunkLoaded(Vector2 chunkPos) {
        String key = String.format("%d,%d", (int) chunkPos.x, (int) chunkPos.y);
        return worldData.getChunks().containsKey(key);
    }

    @Override
    public void loadChunk(Vector2 chunkPos) {
        loadOrGenerateChunk((int) chunkPos.x, (int) chunkPos.y);
    }

    @Override
    public List<WorldObject> getVisibleObjects(Rectangle viewBounds) {
        List<WorldObject> visibleObjects = new ArrayList<>();
        Map<String, ChunkData> visibleChunks = getVisibleChunks(viewBounds);
        for (ChunkData chunk : visibleChunks.values()) {
            if (chunk.getObjects() != null) {
                visibleObjects.addAll(chunk.getObjects());
            }
        }
        return visibleObjects;
    }

    @Override
    public Map<String, ChunkData> getVisibleChunks(Rectangle viewBounds) {
        Map<String, ChunkData> visibleChunks = new HashMap<>();

        int startChunkX = (int) Math.floor(viewBounds.x / (CHUNK_SIZE * TILE_SIZE));
        int startChunkY = (int) Math.floor(viewBounds.y / (CHUNK_SIZE * TILE_SIZE));
        int endChunkX = (int) Math.ceil((viewBounds.x + viewBounds.width) / (CHUNK_SIZE * TILE_SIZE));
        int endChunkY = (int) Math.ceil((viewBounds.y + viewBounds.height) / (CHUNK_SIZE * TILE_SIZE));

        for (int x = startChunkX; x <= endChunkX; x++) {
            for (int y = startChunkY; y <= endChunkY; y++) {
                String key = x + "," + y;
                if (!worldData.getChunks().containsKey(key)) {
                    loadOrGenerateChunk(x, y);
                }
                ChunkData chunk = worldData.getChunks().get(key);
                if (chunk != null) {
                    visibleChunks.put(key, chunk);
                }
            }
        }

        return visibleChunks;
    }

    @Override
    public void setPlayerData(PlayerData playerData) {
        worldData.getPlayers().put(playerData.getUsername(), playerData);
        playerDataRepository.save(playerData);
    }

    @Override
    public PlayerData getPlayerData(String username) {
        PlayerData pd = worldData.getPlayers().get(username);
        if (pd == null) {
            pd = playerDataRepository.findByUsername(username);
            if (pd == null) {
                pd = new PlayerData(username, 0, 0, worldData);
                playerDataRepository.save(pd);
            }
            worldData.getPlayers().put(username, pd);
        }
        return pd;
    }

    @Override
    public List<String> getAvailableWorlds() {
        List<String> worlds = new ArrayList<>();
        worldMetadataRepo.findAll().forEach(meta -> worlds.add(meta.getWorldName()));
        return worlds;
    }

    @Override
    public void deleteWorld(String worldName) {
        if (worldMetadataRepo.findById(worldName).isEmpty()) {
            logger.warn("World '{}' does not exist in Cassandra, cannot delete (server)", worldName);
            return;
        }

        worldMetadataRepo.deleteById(worldName);

        if (worldData.getWorldName() != null && worldData.getWorldName().equals(worldName)) {
            worldData.setWorldName(null);
            worldData.setSeed(0);
            worldData.getPlayers().clear();
            worldData.getChunks().clear();
            worldData.setCreatedDate(0);
            worldData.setLastPlayed(0);
            worldData.setPlayedTime(0);
            logger.info("Cleared current loaded world data because it was deleted (server).");
        }
        logger.info("Deleted world '{}' from Cassandra (server)", worldName);
    }

    @Override
    public void regenerateChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        worldData.getChunks().remove(key);
        chunkRepository.deleteById(new ChunkData.ChunkKey(chunkX, chunkY));
        loadOrGenerateChunk(chunkX, chunkY);
    }

    @Override
    public void generateWorldThumbnail(String worldName) {
        logger.info("Skipping world thumbnail generation on server.");
    }

    // Implementations of the missing methods

    @Override
    public void loadOrReplaceChunkData(int chunkX, int chunkY, int[][] tiles, List<WorldObject> objects) {
        String key = chunkX + "," + chunkY;
        ChunkData cData = new ChunkData();
        cData.setKey(new ChunkData.ChunkKey(chunkX, chunkY));
        cData.setTiles(tiles);
        cData.setObjects(objects);
        worldData.getChunks().put(key, cData);
        logger.info("Replaced/Loaded chunk ({}, {}) with {} objects.", chunkX, chunkY, objects != null ? objects.size() : 0);
    }

    @Override
    public void updateWorldObjectState(WorldObjectUpdate update) {
        String key = (update.getTileX() / 16) + "," + (update.getTileY() / 16);
        ChunkData chunk = worldData.getChunks().get(key);
        if (chunk == null) {
            logger.warn("No chunk found for ({}, {}) to update object {}.", update.getTileX()/16, update.getTileY()/16, update.getObjectId());
            return;
        }

        List<WorldObject> objs = chunk.getObjects();
        if (objs == null) {
            objs = new ArrayList<>();
            chunk.setObjects(objs);
        }

        if (update.isRemoved()) {
            boolean removed = objs.removeIf(o -> o.getId().equals(update.getObjectId()));
            if (removed) {
                logger.info("Removed object {} from chunk {}", update.getObjectId(), key);
            }
        } else {
            // Check if object already exists
            boolean found = false;
            for (WorldObject wo : objs) {
                if (wo.getId().equals(update.getObjectId())) {
                    wo.setTileX(update.getTileX());
                    wo.setTileY(update.getTileY());
                    found = true;
                    logger.info("Updated object {} position in chunk {}", update.getObjectId(), key);
                    break;
                }
            }
            if (!found) {
                try {
                    ObjectType objType = ObjectType.valueOf(update.getType());
                    WorldObject newObj = new WorldObject(
                            update.getTileX(),
                            update.getTileY(),
                            objType,
                            objType.isCollidable()
                    );
                    objs.add(newObj);
                    logger.info("Added new object {} of type {} in chunk {}", update.getObjectId(), update.getType(), key);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid object type '{}' for new object '{}'", update.getType(), update.getObjectId());
                }
            }
        }
    }

    @Override
    public OrthographicCamera getCamera() {
        // Server typically does not have a camera.
        return camera;
    }

    @Override
    public void setCamera(OrthographicCamera camera) {
        // No-op for server, we can store it but it has no real use here.
        this.camera = camera;
    }
}

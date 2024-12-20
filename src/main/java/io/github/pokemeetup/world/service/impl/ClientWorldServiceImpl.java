package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.pokemeetup.multiplayer.model.WorldObjectUpdate;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.repository.PlayerDataRepository;
import io.github.pokemeetup.world.biome.config.BiomeConfigurationLoader;
import io.github.pokemeetup.world.biome.model.Biome;
import io.github.pokemeetup.world.biome.model.BiomeType;
import io.github.pokemeetup.world.biome.service.BiomeService;
import io.github.pokemeetup.world.config.WorldConfig;
import io.github.pokemeetup.world.model.*;
import io.github.pokemeetup.world.repository.ChunkRepository;
import io.github.pokemeetup.world.repository.WorldMetadataRepository;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldGenerator;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Profile("client")
public class ClientWorldServiceImpl extends BaseWorldServiceImpl implements WorldService {
    private static final int CHUNK_SIZE = 16;
    private static final int TILE_SIZE = 32;
    private final WorldConfig worldConfig;
    private final WorldGenerator worldGenerator;
    private final WorldObjectManager worldObjectManager;
    private final TileManager tileManager;
    private final ObjectTextureManager objectTextureManager;
    private final BiomeConfigurationLoader biomeLoader;
    private final BiomeService biomeService;
    private final WorldMetadataRepository worldMetadataRepo;
    private final ChunkRepository chunkRepository;
    private final PlayerDataRepository playerDataRepository;
    private final WorldData worldData = new WorldData();
    @Value("${world.defaultName:defaultWorld}")
    private String defaultWorldName;
    @Value("${world.saveDir:assets/save/worlds/}")
    private String saveDir;
    private boolean initialized = false;
    @Autowired
    @Lazy
    private MultiplayerClient multiplayerClient;
    private boolean isMultiplayerMode = false;

    public ClientWorldServiceImpl(WorldConfig worldConfig,
                                  WorldGenerator worldGenerator,
                                  WorldObjectManager worldObjectManager,
                                  TileManager tileManager,
                                  BiomeConfigurationLoader biomeLoader,
                                  BiomeService biomeService,
                                  WorldMetadataRepository worldMetadataRepo,
                                  ChunkRepository chunkRepository,
                                  PlayerDataRepository playerDataRepository, ObjectTextureManager objectTextureManager) {
        this.worldConfig = worldConfig;
        this.objectTextureManager = objectTextureManager;
        this.worldGenerator = worldGenerator;
        this.worldObjectManager = worldObjectManager;
        this.tileManager = tileManager;
        this.biomeLoader = biomeLoader;
        this.biomeService = biomeService;
        this.worldMetadataRepo = worldMetadataRepo;
        this.chunkRepository = chunkRepository;
        this.playerDataRepository = playerDataRepository;
    }

    public void setMultiplayerMode(boolean multiplayer) {
        this.isMultiplayerMode = multiplayer;
    }

    @Override
    public void loadOrReplaceChunkData(int chunkX, int chunkY, int[][] tiles, List<WorldObject> objects) {
        String key = chunkX + "," + chunkY;
        ChunkData cData = new ChunkData();
        cData.setKey(new ChunkData.ChunkKey(chunkX, chunkY));
        cData.setTiles(tiles);
        cData.setObjects(objects);
        getWorldData().getChunks().put(key, cData);
    }

    @Override

    public void updateWorldObjectState(WorldObjectUpdate update) {
        String key = (update.getTileX() / 16) + "," + (update.getTileY() / 16);
        ChunkData chunk = getWorldData().getChunks().get(key);
        if (chunk == null) return; // Chunk not loaded yet?

        List<WorldObject> objs = chunk.getObjects();
        if (update.isRemoved()) {
            objs.removeIf(o -> o.getId().equals(update.getObjectId()));
        } else {
            boolean found = false;
            for (WorldObject wo : objs) {
                if (wo.getId().equals(update.getObjectId())) {
                    wo.setTileX(update.getTileX());
                    wo.setTileY(update.getTileY());
                    found = true;
                    break;
                }
            }
            if (!found) {
                ObjectType objType = ObjectType.valueOf(update.getType());
                WorldObject newObj = new WorldObject(
                        update.getTileX(),
                        update.getTileY(),
                        objType,
                        objType.isCollidable()
                );
                objs.add(newObj);
            }
        }
    }

    @Override
    public TileManager getTileManager() {
        return this.tileManager;
    }

    @Override
    public void initIfNeeded() {
        if (!initialized) {
            Map<BiomeType, Biome> biomes = biomeLoader.loadBiomes("assets/config/biomes.json");

            if (worldData.getSeed() == 0) {
                worldData.setSeed(worldConfig.getSeed());
            }

            long seed = worldData.getSeed();
            worldGenerator.setSeedAndBiomes(seed, biomes);
            biomeService.initWithSeed(seed);

            worldObjectManager.initialize();
            tileManager.initIfNeeded();
            initialized = true;
            log.info("WorldService (client) initialized with seed {}", seed);
        }
    }

    @Override
    public WorldData getWorldData() {
        return worldData;
    }

    @Override
    public boolean createWorld(String worldName, long seed) {
        if (worldMetadataRepo.findById(worldName).isPresent()) {
            log.warn("World '{}' already exists, cannot create", worldName);
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

        log.info("Created new world '{}' with seed {}", worldName, seed);
        return true;
    }

    @Override
    public void saveWorldData() {
        if (worldData.getWorldName() == null || worldData.getWorldName().isEmpty()) {
            log.info("No world loaded, nothing to save.");
            return;
        }

        Optional<WorldMetadata> optionalMeta = worldMetadataRepo.findById(worldData.getWorldName());
        WorldMetadata meta = optionalMeta.orElseGet(WorldMetadata::new);

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
            ChunkData.ChunkKey cKey = new ChunkData.ChunkKey(cx, cy);
            cData.setKey(cKey);
            chunkRepository.save(cData);
        }

        log.info("Saved world data for '{}'", worldData.getWorldName());
    }

    @Override
    public void loadWorld(String worldName) {
        Optional<WorldMetadata> optionalMeta = worldMetadataRepo.findById(worldName);
        if (optionalMeta.isEmpty()) {
            log.warn("No such world '{}'", worldName);
            return;
        }

        WorldMetadata meta = optionalMeta.get();
        worldData.setWorldName(meta.getWorldName());
        worldData.setSeed(meta.getSeed());
        worldData.setCreatedDate(meta.getCreatedDate());
        worldData.setLastPlayed(System.currentTimeMillis());
        worldData.setPlayedTime(meta.getPlayedTime());

        initIfNeeded();

        playerDataRepository.findAll().forEach(pd -> {
            worldData.getPlayers().put(pd.getUsername(), pd);
        });

        log.info("Loaded world data for world: {}", worldName);
    }

    @Override
    public void loadWorldData() {
        Optional<WorldMetadata> optMeta = worldMetadataRepo.findById(defaultWorldName);
        if (optMeta.isEmpty()) {
            log.warn("No default world '{}' found in Cassandra. Please create a world or specify another default world.", defaultWorldName);
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

        log.info("Loaded default world data for '{}' from Cassandra", defaultWorldName);
    }

    @Override
    public int[][] getChunkTiles(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        ChunkData cData = getWorldData().getChunks().get(key);
        if (cData == null) {
            if (isMultiplayerMode) {
                // In multiplayer mode, do NOT generate locally. Just request and return null for now.
                multiplayerClient.requestChunk(chunkX, chunkY);
                return null;
            } else {
                // Singleplayer mode: generate or load chunk locally as before
                loadOrGenerateChunk(chunkX, chunkY);
                cData = getWorldData().getChunks().get(key);
            }
        }
        return cData != null ? cData.getTiles() : null;
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
                ChunkData chunk = worldData.getChunks().get(key);
                if (chunk == null) {
                    // Request it from server if not already requested
                    multiplayerClient.requestChunk(x, y);
                    continue; // It's not ready yet
                }
                visibleChunks.put(key, chunk);
            }
        }

        return visibleChunks;
    }

    private void loadOrGenerateChunk(int chunkX, int chunkY) {
        if (isMultiplayerMode) {
            // Do nothing in multiplayer mode. We rely solely on server data.
            return;
        }

        // Original singleplayer logic here:
        String key = chunkX + "," + chunkY;
        Optional<ChunkData> opt = chunkRepository.findById(new ChunkData.ChunkKey(chunkX, chunkY));
        if (opt.isPresent()) {
            ChunkData cData = opt.get();
            if (cData.getObjects() != null) {
                cData.setObjects(new ArrayList<>(cData.getObjects()));
            }
            worldObjectManager.loadObjectsForChunk(chunkX, chunkY, cData.getObjects());
            getWorldData().getChunks().put(key, cData);
            return;
        }

        int[][] tiles = worldGenerator.generateChunk(chunkX, chunkY);
        ChunkData cData = new ChunkData();
        cData.setKey(new ChunkData.ChunkKey(chunkX, chunkY));
        cData.setTiles(tiles);
        Biome biome = worldGenerator.getBiomeForChunk(chunkX, chunkY);
        List<WorldObject> objs = worldObjectManager.generateObjectsForChunk(chunkX, chunkY, tiles, biome, getWorldData().getSeed());
        cData.setObjects(objs);

        getWorldData().getChunks().put(key, cData);
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
                for (WorldObject obj : chunk.getObjects()) {
                    float pixelX = obj.getTileX() * TILE_SIZE;
                    float pixelY = obj.getTileY() * TILE_SIZE;
                    if (viewBounds.contains(pixelX, pixelY)) {
                        visibleObjects.add(obj);
                    }
                }
            }
        }
        return visibleObjects;
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
                pd = new PlayerData(username, 0, 0);
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
        worldMetadataRepo.deleteById(worldName);

        if (worldData.getWorldName() != null && worldData.getWorldName().equals(worldName)) {
            worldData.setWorldName(null);
            worldData.setSeed(0);
            worldData.getPlayers().clear();
            worldData.getChunks().clear();
            worldData.setCreatedDate(0);
            worldData.setLastPlayed(0);
            worldData.setPlayedTime(0);
            log.info("Cleared current loaded world data because it was deleted.");
        }
        log.info("Deleted world '{}'", worldName);
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
        initIfNeeded();
        objectTextureManager.initializeIfNeeded();
        int previewSize = 8;
        int tileSize = 32;
        int iconWidth = 128;
        int iconHeight = 128;

        float scaleX = (float) iconWidth / (previewSize * tileSize);
        float scaleY = (float) iconHeight / (previewSize * tileSize);
        float scale = Math.min(scaleX, scaleY);

        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, iconWidth, iconHeight, false);
        SpriteBatch batch = new SpriteBatch();

        fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        OrthographicCamera camera = new OrthographicCamera(iconWidth, iconHeight);
        camera.setToOrtho(true, iconWidth, iconHeight);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        com.badlogic.gdx.math.Matrix4 transform = batch.getTransformMatrix();
        transform.idt();

        float worldWidth = previewSize * tileSize;
        float worldHeight = previewSize * tileSize;
        transform.translate(iconWidth / 2f, iconHeight / 2f, 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-worldWidth / 2f, -worldHeight / 2f, 0);

        batch.setTransformMatrix(transform);

        int centerX = 0;
        int centerY = 0;


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
                            float worldPixelX = (objTileX - (centerX - (float) previewSize / 2)) * tileSize;
                            float worldPixelY = (objTileY - (centerY - (float) previewSize / 2)) * tileSize;


                            TextureRegion objTexture = objectTextureManager.getTexture(obj.getType().getTextureRegionName());
                            if (objTexture != null) {
                                batch.draw(objTexture, worldPixelX, worldPixelY,
                                        obj.getType().getWidthInTiles() * tileSize,
                                        obj.getType().getHeightInTiles() * tileSize);
                            }
                        }
                    }
                    processedChunks.add(key);
                }
            }
        }

        batch.end();

        Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, iconWidth, iconHeight);
        fbo.end();


        FileHandle dir = Gdx.files.local(saveDir + worldName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileHandle iconFile = Gdx.files.local(saveDir + worldName + "/icon.png");
        PixmapIO.writePNG(iconFile, pm);

        pm.dispose();
        batch.dispose();
        fbo.dispose();

        log.info("Generated world thumbnail for '{}'", worldName);
    }

}

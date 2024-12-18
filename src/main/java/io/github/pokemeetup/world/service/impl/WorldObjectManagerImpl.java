package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.pokemeetup.world.model.Biome;
import io.github.pokemeetup.world.model.ObjectType;
import io.github.pokemeetup.world.model.WorldObject;
import io.github.pokemeetup.world.service.WorldObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WorldObjectManagerImpl implements WorldObjectManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldObjectManagerImpl.class);
    private void rebindTexturesForObjects(List<WorldObject> objects) {
        for (WorldObject obj : objects) {
            TextureRegion tex = objectTextures.get(obj.getType());
            obj.setTexture(tex);
        }
    }

    @Override
    public void loadObjectsForChunk(int chunkX, int chunkY, List<WorldObject> objects) {
        String key = chunkX + "," + chunkY;
        rebindTexturesForObjects(objects);
        objectsByChunk.put(key, objects);
    }

    private final Map<String, List<WorldObject>> objectsByChunk = new ConcurrentHashMap<>();
    private TextureAtlas atlas;
    private final Map<ObjectType, TextureRegion> objectTextures = new HashMap<>();

    @Override
    public void initialize() {
        if (atlas == null) {
            FileHandle atlasFile = Gdx.files.internal("assets/atlas/tiles-gfx-atlas");
            atlas = new TextureAtlas(atlasFile);
            loadTextures();
        }
    }

    private void loadTextures() {
        objectTextures.put(ObjectType.TREE_0, atlas.findRegion("tree_0"));
        objectTextures.put(ObjectType.TREE_1, atlas.findRegion("tree_1"));
        objectTextures.put(ObjectType.CACTUS, atlas.findRegion("cactus"));
        objectTextures.put(ObjectType.BUSH, atlas.findRegion("bush"));
        objectTextures.put(ObjectType.SUNFLOWER, atlas.findRegion("sunflower"));
        objectTextures.put(ObjectType.APRICORN_TREE, atlas.findRegion("apricorn_tree"));
        objectTextures.put(ObjectType.DEAD_TREE, atlas.findRegion("dead_tree"));
        logger.info("WorldObject textures loaded successfully.");
    }

    @Override
    public List<WorldObject> generateObjectsForChunk(int chunkX, int chunkY, int[][] tiles, Biome biome, long seed) {
        String key = chunkX + "," + chunkY;

        if (biome == null || biome.getSpawnableObjects().isEmpty()) {
            objectsByChunk.put(key, Collections.emptyList());
            return Collections.emptyList();
        }

        List<WorldObject> objects = new CopyOnWriteArrayList<>();

        // Deterministic random
        Random random = new Random((chunkX * 341L + chunkY * 773L) ^ seed);

        int chunkSize = tiles.length;

        for (String objName : biome.getSpawnableObjects()) {
            ObjectType type;
            try {
                type = ObjectType.valueOf(objName);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown object type: {} in biome {}", objName, biome.getType());
                continue;
            }

            double chance = biome.getSpawnChanceForObject(type);
            int attempts = (int) (chance * (chunkSize * chunkSize));

            for (int i = 0; i < attempts; i++) {
                int lx = random.nextInt(chunkSize);
                int ly = random.nextInt(chunkSize);

                int tileType = tiles[lx][ly];
                if (!biome.getAllowedTileTypes().contains(tileType)) {
                    continue;
                }

                if (canPlaceObject(objects, chunkX, chunkY, lx, ly)) {
                    TextureRegion tex = objectTextures.get(type);
                    if (tex == null) {
                        continue;
                    }

                    int worldX = chunkX * chunkSize + lx;
                    int worldY = chunkY * chunkSize + ly;
                    WorldObject obj = new WorldObject(worldX, worldY, type, tex);
                    objects.add(obj);
                }
            }
        }

        objectsByChunk.put(key, objects);
        logger.info("Generated {} objects for chunk {},{} using biome {}", objects.size(), chunkX, chunkY, biome.getType());
        return objects;
    }

    private boolean canPlaceObject(List<WorldObject> currentObjects, int chunkX, int chunkY, int lx, int ly) {
        int worldX = chunkX * 16 + lx; // assuming chunk size=16
        int worldY = chunkY * 16 + ly;
        for (WorldObject obj : currentObjects) {
            if (obj.getTileX() == worldX && obj.getTileY() == worldY) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<WorldObject> getObjectsForChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        return objectsByChunk.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public void addObject(WorldObject object) {
        int chunkX = object.getTileX() / 16; // assuming chunk size=16
        int chunkY = object.getTileY() / 16;
        String key = chunkX + "," + chunkY;
        objectsByChunk.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(object);
    }

    @Override
    public void removeObject(String objectId) {
        for (List<WorldObject> objs : objectsByChunk.values()) {
            objs.removeIf(o -> o.getId().equals(objectId));
        }
    }
}

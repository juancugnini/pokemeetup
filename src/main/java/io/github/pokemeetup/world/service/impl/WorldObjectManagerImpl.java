package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WorldObjectManagerImpl implements WorldObjectManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldObjectManagerImpl.class);

    // Store objects by chunk coordinates
    // key format: chunkX+","+chunkY
    private final Map<String, List<WorldObject>> objectsByChunk = new ConcurrentHashMap<>();

    private TextureAtlas atlas;
    private final Map<ObjectType, TextureRegion> objectTextures = new HashMap<>();

    public WorldObjectManagerImpl() {
        loadTextures();
    }

    private void loadTextures() {
        try {
            atlas = new TextureAtlas("assets/atlas/tiles-gfx-atlas");
            objectTextures.put(ObjectType.TREE_0, atlas.findRegion("tree_0"));
            objectTextures.put(ObjectType.TREE_1, atlas.findRegion("tree_1"));
            objectTextures.put(ObjectType.CACTUS, atlas.findRegion("cactus"));
            objectTextures.put(ObjectType.BUSH, atlas.findRegion("bush"));
            objectTextures.put(ObjectType.SUNFLOWER, atlas.findRegion("sunflower"));
            objectTextures.put(ObjectType.APRICORN_TREE, atlas.findRegion("apricorn_tree"));
            objectTextures.put(ObjectType.DEAD_TREE, atlas.findRegion("dead_tree"));
        } catch (Exception e) {
            logger.error("Failed to load object textures: {}", e.getMessage());
        }
    }

    @Override
    public void generateObjectsForChunk(int chunkX, int chunkY, int[][] tiles, Biome biome) {
        List<WorldObject> objects = new CopyOnWriteArrayList<>();

        for (String objName : biome.getSpawnableObjects()) {
            ObjectType type;
            try {
                type = ObjectType.valueOf(objName);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown object type: {}", objName);
                continue;
            }

            double chance = biome.getSpawnChanceForObject(type);
            int attempts = (int) (chance * (tiles.length * tiles[0].length));

            Random random = new Random((chunkX * 341 + chunkY * 773) ^ System.currentTimeMillis());
            for (int i = 0; i < attempts; i++) {
                int lx = random.nextInt(tiles.length);
                int ly = random.nextInt(tiles[0].length);

                int tileType = tiles[lx][ly];
                if (!biome.getAllowedTileTypes().contains(tileType)) {
                    continue;
                }

                if (canPlaceObject(objects, chunkX, chunkY, lx, ly)) {
                    TextureRegion tex = objectTextures.get(type);
                    if (tex != null) {
                        int worldX = chunkX * tiles.length + lx;
                        int worldY = chunkY * tiles[0].length + ly;
                        WorldObject obj = new WorldObject(worldX, worldY, type, tex);
                        objects.add(obj);
                    }
                }
            }
        }
        String key = chunkX + "," + chunkY;
        objectsByChunk.put(key, objects);
    }

    private boolean canPlaceObject(List<WorldObject> currentObjects, int chunkX, int chunkY, int lx, int ly) {
        for (WorldObject obj : currentObjects) {
            if (obj.getTileX() == (chunkX * 16 + lx) && obj.getTileY() == (chunkY * 16 + ly)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addObject(WorldObject object) {
        int chunkX = object.getTileX() / 16;
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

    @Override
    public List<WorldObject> getObjectsForChunk(int chunkX, int chunkY) {
        String key = chunkX + "," + chunkY;
        return objectsByChunk.getOrDefault(key, Collections.emptyList());
    }
}

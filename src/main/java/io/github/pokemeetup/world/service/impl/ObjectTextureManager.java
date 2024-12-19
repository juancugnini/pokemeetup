package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class ObjectTextureManager {

    private static final Logger logger = LoggerFactory.getLogger(ObjectTextureManager.class);

    private static final String ATLAS_PATH = "assets/atlas/tiles-gfx-atlas";

    private TextureAtlas atlas;
    private final Map<String, TextureRegion> regionCache = new HashMap<>();
    private boolean initialized = false;

    
    public void initializeIfNeeded() {
        if (initialized) return;

        if (Gdx.files == null) {
            logger.warn("Gdx environment not ready. Cannot load atlas yet.");
            return;
        }

        if (!Gdx.files.internal(ATLAS_PATH).exists()) {
            logger.error("Atlas file not found at: {}", ATLAS_PATH);
        } else {
            atlas = new TextureAtlas(Gdx.files.internal(ATLAS_PATH));
            logger.info("ObjectTextureManager initialized with atlas: {}", ATLAS_PATH);
            initialized = true;
        }
    }

    
    public TextureRegion getTexture(String name) {
        if (!initialized || atlas == null) {
            logger.warn("ObjectTextureManager not initialized or atlas not loaded. Name: {}", name);
            return null;
        }


        if (regionCache.containsKey(name)) {
            return regionCache.get(name);
        }

        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            logger.warn("No region found for '{}', trying 'unknown' texture.", name);
            region = atlas.findRegion("unknown");
            if (region == null) {
                logger.error("No 'unknown' region found in atlas. Unable to provide fallback.");
                return null;
            }
        }

        regionCache.put(name, region);
        return region;
    }

    public void disposeTextures() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
            logger.info("ObjectTextureManager atlas disposed.");
        }
    }

}

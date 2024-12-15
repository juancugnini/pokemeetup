package io.github.pokemeetup.world.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import io.github.pokemeetup.world.config.TileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class TileManager {
    private static final Logger logger = LoggerFactory.getLogger(TileManager.class);

    @Value("${tiles.configPath:config/tiles.json}")
    private String tileConfigFile;

    private final HashMap<Integer, TileConfig.TileDefinition> tiles = new HashMap<>();
    private TextureAtlas atlas;

    @PostConstruct
    public void init() {
        loadConfig(tileConfigFile);
        
        atlas = new TextureAtlas(Gdx.files.internal("assets/atlas/tiles-gfx-atlas"));
        logger.info("Loaded tile atlas with {} regions.", atlas.getRegions().size);
    }

    private void loadConfig(String tileConfigFile) {
        FileHandle file = Gdx.files.internal(tileConfigFile);
        if(!file.exists()) {
            logger.error("Tile config file not found: {}", tileConfigFile);
            return;
        }
        Json json = new Json();
        TileConfig config = json.fromJson(TileConfig.class, file.readString());
        for (TileConfig.TileDefinition def : config.getTiles()) {
            tiles.put(def.getId(), def);
        }
        logger.info("Loaded {} tiles from config.", tiles.size());
    }

    
    public TextureRegion getRegionForTile(int tileId) {
        TileConfig.TileDefinition def = tiles.get(tileId);
        if (def == null) {
            return atlas.findRegion("unknown"); 
        }
        TextureRegion region = atlas.findRegion(def.getTexture());
        if (region == null) {
            logger.warn("No region found in atlas for tile texture: {}", def.getTexture());
            return atlas.findRegion("unknown");
        }
        return region;
    }

    public boolean isPassable(int tileId) {
        TileConfig.TileDefinition def = tiles.get(tileId);
        if (def == null) return false; 
        return def.isPassable();
    }

    public String getTileName(int tileId) {
        TileConfig.TileDefinition def = tiles.get(tileId);
        if (def == null) return "unknown";
        return def.getName();
    }
}

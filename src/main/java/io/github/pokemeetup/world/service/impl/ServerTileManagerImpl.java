package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.pokemeetup.world.service.TileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("server")
@Qualifier("serverTileManagerImpl")
public class ServerTileManagerImpl implements TileManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerTileManagerImpl.class);

    @Override
    public void initIfNeeded() {
        logger.info("ServerTileManagerImpl initialized (no-op).");
    }

    @Override
    public TextureRegion getRegionForTile(int tileId) {
        return null;
    }

    @Override
    public boolean isPassable(int tileId) {
        return true;
    }

    @Override
    public String getTileName(int tileId) {
        return "unknown";
    }
}

package io.github.pokemeetup.world.service.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.pokemeetup.world.service.TileManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Primary
@Profile("server")
@Qualifier("serverTileManagerImpl")
public class ServerTileManagerImpl implements TileManager {

    @Override
    public void initIfNeeded() {
        log.info("ServerTileManagerImpl initialized (no-op).");
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

package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.model.WorldObject;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GameScreen handles rendering of the game world and player. This is also where we handle the game core loop logic.
 */
@Component
public class GameScreen implements Screen {

    private static final float TARGET_VIEWPORT_WIDTH_TILES = 24f;
    private static final int TILE_SIZE = 32;

    private final PlayerService playerService;
    private final WorldService worldService;
    private final AudioService audioService;
    private final InputService inputService;
    private final TileManager tileManager;
    private final WorldObjectManager worldObjectManager;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    /**
     * Constructor with dependency injection.
     *
     * @param playerService      Service handling player logic.
     * @param worldService       Service handling world data.
     * @param audioService       Service handling audio.
     * @param tileManager        Service managing tile textures.
     * @param worldObjectManager Service managing world objects.
     * @param inputService       Service handling player input.
     */
    @Autowired
    public GameScreen(PlayerService playerService,
                      WorldService worldService,
                      AudioService audioService,
                      TileManager tileManager,
                      WorldObjectManager worldObjectManager,
                      InputService inputService) {
        this.playerService = playerService;
        this.worldService = worldService;
        this.audioService = audioService;
        this.tileManager = tileManager;
        this.worldObjectManager = worldObjectManager;
        this.inputService = inputService;
    }

    /**
     * Initializes the screen components.
     */
    @Override
    public void show() {
        float baseWidth = TARGET_VIEWPORT_WIDTH_TILES * TILE_SIZE;
        float aspect = (float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        float baseHeight = baseWidth * aspect;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, baseWidth, baseHeight);

        batch = new SpriteBatch();
        font = new BitmapFont(); // Consider using a custom font via BitmapFont files

        audioService.playMenuMusic();

        // Register InputService as the input processor
        Gdx.input.setInputProcessor(inputService);
    }

    /**
     * Renders the game world and player.
     *
     * @param delta Time elapsed since the last frame.
     */
    @Override
    public void render(float delta) {
        // Update player movement and animation
        playerService.update(delta);

        // Update camera to follow player
        updateCamera();

        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Begin SpriteBatch for rendering
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Retrieve player tile position
        int playerTileX = (int) (playerService.getPlayerData().getX());
        int playerTileY = (int) (playerService.getPlayerData().getY());

        int viewRadius = 24; // Number of tiles to render around the player

        // Render tiles
        for (int dx = -viewRadius; dx <= viewRadius; dx++) {
            for (int dy = -viewRadius; dy <= viewRadius; dy++) {
                int tileX = playerTileX + dx;
                int tileY = playerTileY + dy;
                int chunkX = tileX / 16;
                int chunkY = tileY / 16;
                int[][] tiles = worldService.getChunkTiles(chunkX, chunkY);

                int localX = Math.floorMod(tileX, 16);
                int localY = Math.floorMod(tileY, 16);
                if (tiles != null && localX >= 0 && localX < 16 && localY >= 0 && localY < 16) {
                    int tileType = tiles[localX][localY];
                    TextureRegion region = tileManager.getRegionForTile(tileType);
                    float worldPixelX = tileX * TILE_SIZE;
                    float worldPixelY = tileY * TILE_SIZE;
                    batch.draw(region, worldPixelX, worldPixelY, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Render world objects
        for (int dx = -viewRadius; dx <= viewRadius; dx++) {
            for (int dy = -viewRadius; dy <= viewRadius; dy++) {
                int tileX = playerTileX + dx;
                int tileY = playerTileY + dy;
                int chunkX = tileX / 16;
                int chunkY = tileY / 16;
                List<WorldObject> objects = worldObjectManager.getObjectsForChunk(chunkX, chunkY);
                for (WorldObject obj : objects) {
                    if (Math.abs(obj.getTileX() - playerTileX) <= viewRadius &&
                            Math.abs(obj.getTileY() - playerTileY) <= viewRadius) {
                        if (obj.getTexture() != null) {
                            batch.draw(obj.getTexture(), obj.getPixelX(), obj.getPixelY());
                        }
                    }
                }
            }
        }

        // Render the player
        playerService.render(batch);

        // End SpriteBatch
        batch.end();
    }

    /**
     * Updates the camera position to follow the player.
     */
    private void updateCamera() {
        float playerPixelX = playerService.getPlayerData().getX() * TILE_SIZE + TILE_SIZE / 2f;
        float playerPixelY = playerService.getPlayerData().getY() * TILE_SIZE + TILE_SIZE / 2f;
        camera.position.set(playerPixelX, playerPixelY, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = TARGET_VIEWPORT_WIDTH_TILES * TILE_SIZE;
        float aspect = (float) height / width;
        camera.viewportHeight = camera.viewportWidth * aspect;
        camera.update();
    }

    @Override
    public void pause() {
        // Handle pause logic if necessary
    }

    @Override
    public void resume() {
        // Handle resume logic if necessary
    }

    /**
     * Stops music when the screen is hidden.
     */
    @Override
    public void hide() {
        audioService.stopMenuMusic();
    }

    /**
     * Disposes of resources when the screen is destroyed.
     */
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

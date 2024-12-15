package io.github.pokemeetup.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.input.InputConfiguration;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.stereotype.Component;

@Component
public class GameScreen implements Screen {

    private static final float TARGET_VIEWPORT_WIDTH_TILES = 24f;
    private static final int TILE_SIZE = 32;

    private final PlayerService playerService;
    private final WorldService worldService;
    private final AudioService audioService;
    private final InputService inputService;
    private final TileManager tileManager;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    public GameScreen(PlayerService playerService, WorldService worldService, AudioService audioService, TileManager tileManager) {
        this.playerService = playerService;
        this.worldService = worldService;
        this.audioService = audioService;
        this.tileManager = tileManager;

        InputConfiguration config = new InputConfiguration();
        this.inputService = new InputService(playerService, config);
    }

    @Override
    public void show() {
        float baseWidth = TARGET_VIEWPORT_WIDTH_TILES * TILE_SIZE;
        float aspect = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        float baseHeight = baseWidth * aspect;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, baseWidth, baseHeight);

        batch = new SpriteBatch();
        font = new BitmapFont();

        audioService.playMenuMusic();
    }

    @Override
    public void render(float delta) {
        
        inputService.update(delta);

        
        playerService.update(delta);

        updateCamera();

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        
        int playerTileX = (int)(playerService.getPlayerData().getX());
        int playerTileY = (int)(playerService.getPlayerData().getY());

        int viewRadius = 24;
        for (int dx = -viewRadius; dx <= viewRadius; dx++) {
            for (int dy = -viewRadius; dy <= viewRadius; dy++) {
                int tileX = playerTileX + dx;
                int tileY = playerTileY + dy;
                int chunkX = tileX / 16;
                int chunkY = tileY / 16;
                int[][] tiles = worldService.getChunkTiles(chunkX, chunkY);

                int localX = Math.floorMod(tileX,16);
                int localY = Math.floorMod(tileY,16);
                if (tiles != null && localX >= 0 && localX < 16 && localY >= 0 && localY < 16) {
                    int tileType = tiles[localX][localY];

                    
                    TextureRegion region = tileManager.getRegionForTile(tileType);

                    float worldPixelX = tileX * TILE_SIZE;
                    float worldPixelY = tileY * TILE_SIZE;

                    
                    batch.draw(region, worldPixelX, worldPixelY, TILE_SIZE, TILE_SIZE);
                }
            }
        }


        playerService.render(batch);

        batch.end();
    }

    private void updateCamera() {
        float playerPixelX = playerService.getPlayerData().getX() * TILE_SIZE + TILE_SIZE / 2f;
        float playerPixelY = playerService.getPlayerData().getY() * TILE_SIZE + TILE_SIZE / 2f;
        camera.position.set(playerPixelX, playerPixelY, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        audioService.stopMenuMusic();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}

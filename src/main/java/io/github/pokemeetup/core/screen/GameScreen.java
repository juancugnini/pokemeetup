package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.service.ScreenManager;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.model.WorldObject;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final ScreenManager screenManager;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    // Pause menu related
    private Stage pauseStage;
    private Skin pauseSkin;
    private Window pauseWindow;
    private boolean paused = false;

    // Camera smoothing
    private float cameraPosX, cameraPosY; // Current camera position
    private static final float CAMERA_LERP_FACTOR = 0.1f; // Smaller is smoother but slower

    @Autowired
    public GameScreen(PlayerService playerService,
                      WorldService worldService,
                      AudioService audioService,
                      TileManager tileManager,
                      WorldObjectManager worldObjectManager,
                      InputService inputService,
                      ScreenManager screenManager) {
        this.playerService = playerService;
        this.worldService = worldService;
        this.audioService = audioService;
        this.tileManager = tileManager;
        this.worldObjectManager = worldObjectManager;
        this.inputService = inputService;
        this.screenManager = screenManager;
    }

    @Override
    public void show() {
        float baseWidth = TARGET_VIEWPORT_WIDTH_TILES * TILE_SIZE;
        float aspect = (float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        float baseHeight = baseWidth * aspect;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, baseWidth, baseHeight);

        batch = new SpriteBatch();
        font = new BitmapFont();

        audioService.playMenuMusic();

        // Set input to the game input service
        Gdx.input.setInputProcessor(inputService);

        // Initialize pause menu UI
        pauseStage = new Stage(new ScreenViewport());
        pauseSkin = new Skin(Gdx.files.internal("assets/Skins/uiskin.json"));

        pauseWindow = createPauseWindow();
        pauseWindow.setVisible(false);
        pauseStage.addActor(pauseWindow);

        // Initialize camera position to player's position immediately
        float playerPixelX = playerService.getPlayerData().getX() * TILE_SIZE + TILE_SIZE / 2f;
        float playerPixelY = playerService.getPlayerData().getY() * TILE_SIZE + TILE_SIZE / 2f;
        cameraPosX = playerPixelX;
        cameraPosY = playerPixelY;
    }

    private Window createPauseWindow() {
        Window window = new Window("Paused", pauseSkin);

        // Make the background semi-transparent to see the world behind
        // Use the default "window" drawable and tint it
        window.setColor(1f, 1f, 1f, 0.7f);
        // Another approach: create a drawable with transparent color
        // Drawable bg = pauseSkin.newDrawable("white", new Color(0,0,0,0.5f));
        // window.setBackground(bg);

        window.setModal(true);
        window.setMovable(false);

        TextButton resumeButton = new TextButton("Resume", pauseSkin);
        TextButton saveButton = new TextButton("Save Game", pauseSkin);
        TextButton quitButton = new TextButton("Quit to Main Menu", pauseSkin);

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                togglePause();
            }
        });

        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Save world data
                worldService.saveWorldData();
            }
        });

        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Save before quitting
                worldService.saveWorldData();
                paused = false;
                Gdx.input.setInputProcessor(null);
                screenManager.showScreen(ModeSelectionScreen.class);
            }
        });

        Table table = new Table(pauseSkin);
        table.defaults().pad(10).width(200).height(50);
        table.add(resumeButton).row();
        table.add(saveButton).row();
        table.add(quitButton).row();

        window.add(table).pad(20);

        // Position will be updated in resize()
        return window;
    }

    private void togglePause() {
        paused = !paused;
        pauseWindow.setVisible(paused);
        if (paused) {
            Gdx.input.setInputProcessor(pauseStage);
        } else {
            Gdx.input.setInputProcessor(inputService);
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!paused) {
            playerService.update(delta);
            updateCamera(delta);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            renderWorld();
            playerService.render(batch);
            batch.end();
        } else {
            pauseStage.act(delta);
            pauseStage.draw();
        }
    }

    private void renderWorld() {
        int playerTileX = (int) (playerService.getPlayerData().getX());
        int playerTileY = (int) (playerService.getPlayerData().getY());
        int viewRadius = 24;

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
                if (objects != null) {
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
        }
    }

    private void updateCamera(float delta) {
        float playerPixelX = playerService.getPlayerData().getX() * TILE_SIZE + TILE_SIZE / 2f;
        float playerPixelY = playerService.getPlayerData().getY() * TILE_SIZE + TILE_SIZE / 2f;

        // Smoothly move camera towards the player
        cameraPosX = lerp(cameraPosX, playerPixelX, CAMERA_LERP_FACTOR);
        cameraPosY = lerp(cameraPosY, playerPixelY, CAMERA_LERP_FACTOR);

        camera.position.set(cameraPosX, cameraPosY, 0);
        camera.update();
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = TARGET_VIEWPORT_WIDTH_TILES * TILE_SIZE;
        float aspect = (float) height / width;
        camera.viewportHeight = camera.viewportWidth * aspect;
        camera.update();
        pauseStage.getViewport().update(width, height, true);

        // Re-center pause window after resize
        pauseWindow.setPosition(
                (width - pauseWindow.getWidth()) / 2f,
                (height - pauseWindow.getHeight()) / 2f
        );
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // When switching away from the game screen, stop menu music
        audioService.stopMenuMusic();
        paused = false;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        pauseStage.dispose();
        pauseSkin.dispose();
    }
}

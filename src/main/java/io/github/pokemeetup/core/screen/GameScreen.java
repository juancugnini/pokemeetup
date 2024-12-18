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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.service.ScreenManager;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.model.PlayerData;
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

    // Pause menu
    private Stage pauseStage;
    private Skin pauseSkin;
    private Window pauseWindow;
    private boolean paused = false;

    // Camera smoothing
    private float cameraPosX, cameraPosY;
    private static final float CAMERA_LERP_FACTOR = 0.1f;

    // Overlay behind pause menu
    private Image overlay;

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
        Gdx.input.setInputProcessor(inputService);

        pauseStage = new Stage(new ScreenViewport());
        pauseSkin = new Skin(Gdx.files.internal("assets/Skins/uiskin.json"));

        overlay = new Image(pauseSkin.newDrawable("white", new Color(0,0,0,0.5f)));
        overlay.setFillParent(true);
        overlay.setVisible(false);
        pauseStage.addActor(overlay);

        pauseWindow = createPauseWindow();
        pauseWindow.setVisible(false);
        pauseStage.addActor(pauseWindow);

        // Attempt to get player data
        PlayerData pData = worldService.getPlayerData(playerService.getPlayerData().getUsername());
        if (pData == null) {
            // If no player data found, set a default position
            playerService.setPosition(0, 0);
        } else {
            playerService.setPosition((int)pData.getX(), (int)pData.getY());
        }

        // Use current player position for camera
        float px = playerService.getPlayerData().getX() * TILE_SIZE + TILE_SIZE / 2f;
        float py = playerService.getPlayerData().getY() * TILE_SIZE + TILE_SIZE / 2f;
        cameraPosX = px;
        cameraPosY = py;
    }

    private Window createPauseWindow() {
        Window window = new Window("Paused", pauseSkin);
        Drawable bg = pauseSkin.newDrawable("white", new Color(0.2f,0.2f,0.2f,0.8f));
        window.setBackground(bg);

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
                worldService.saveWorldData();
                showSaveFeedback();
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
        window.pack();

        return window;
    }

    private void showSaveFeedback() {
        Label savedLabel = new Label("Game Saved!", pauseSkin);
        savedLabel.setColor(Color.GREEN);
        savedLabel.setPosition(pauseWindow.getX() + pauseWindow.getWidth()/2f - savedLabel.getPrefWidth()/2f,
                pauseWindow.getY() + pauseWindow.getHeight() + 10);

        pauseStage.addActor(savedLabel);
        // Fade out and remove after 1.5s
        savedLabel.addAction(Actions.sequence(
                Actions.delay(1.5f),
                Actions.fadeOut(0.5f),
                Actions.removeActor()
        ));
    }

    private void togglePause() {
        paused = !paused;
        pauseWindow.setVisible(paused);
        overlay.setVisible(paused);
        if (paused) {
            Gdx.input.setInputProcessor(pauseStage);
            centerPauseWindow();
        } else {
            Gdx.input.setInputProcessor(inputService);
        }
    }

    private void centerPauseWindow() {
        pauseWindow.pack();
        float stageWidth = pauseStage.getViewport().getWorldWidth();
        float stageHeight = pauseStage.getViewport().getWorldHeight();

        if (pauseWindow.getWidth() > stageWidth*0.9f) {
            pauseWindow.setWidth(stageWidth*0.9f);
        }
        if (pauseWindow.getHeight() > stageHeight*0.9f) {
            pauseWindow.setHeight(stageHeight*0.9f);
        }
        pauseWindow.setPosition((stageWidth - pauseWindow.getWidth())/2f,
                (stageHeight - pauseWindow.getHeight())/2f);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateCamera(delta);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderWorld();
        playerService.render(batch);
        batch.end();

        if (paused) {
            pauseStage.act(delta);
            pauseStage.draw();
        } else {
            playerService.update(delta);
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

        if (paused) {
            centerPauseWindow();
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
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

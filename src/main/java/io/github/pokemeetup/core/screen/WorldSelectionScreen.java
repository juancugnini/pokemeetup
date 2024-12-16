package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.stereotype.Component;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.List;

@Component
public class WorldSelectionScreen implements Screen {
    private final AudioService audioService;
    private final WorldService worldService;
    private final PlayerService playerService;

    private Stage stage;
    private Skin skin;
    private Table worldListTable;
    private String selectedWorldName;

    public WorldSelectionScreen(AudioService audioService, WorldService worldService, PlayerService playerService) {
        this.audioService = audioService;
        this.worldService = worldService;
        this.playerService = playerService;
    }

    @Override
    public void show() {
        audioService.playMenuMusic();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("assets/Skins/uiskin.json"));

        Table root = new Table(skin);
        root.setFillParent(true);

        Label title = new Label("Select World", skin);
        root.add(title).pad(10).row();

        // Load world names from WorldService
        List<String> worldNames = worldService.getAvailableWorlds();
        worldListTable = new Table(skin);

        for (String worldName : worldNames) {
            Table entry = createWorldEntry(worldName);
            worldListTable.add(entry).expandX().fillX().row();
        }

        ScrollPane scrollPane = new ScrollPane(worldListTable, skin);
        root.add(scrollPane).expand().fill().row();

        TextButton createButton = new TextButton("Create New World", skin);
        TextButton playButton = new TextButton("Play Selected World", skin);
        TextButton deleteButton = new TextButton("Delete World", skin);
        TextButton backButton = new TextButton("Back", skin);

        Table buttonTable = new Table(skin);
        buttonTable.add(createButton).pad(5);
        buttonTable.add(playButton).pad(5);
        buttonTable.add(deleteButton).pad(5);
        buttonTable.row();
        buttonTable.add(backButton).colspan(3).center().pad(5);

        root.add(buttonTable).padTop(20);

        stage.addActor(root);

        createButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                // Show Create World Dialog (not fully implemented here)
            }
            return false;
        });

        playButton.addListener(event -> {
            if (event.toString().equals("touchDown") && selectedWorldName != null) {
                // Load world via WorldService using the selected world name
                worldService.loadWorld(selectedWorldName);
                // Switch screen to GameScreen (not shown here, depends on your screen management)
            }
            return false;
        });

        deleteButton.addListener(event -> {
            if (event.toString().equals("touchDown") && selectedWorldName != null) {
                // Assuming worldService has a deleteWorld(String worldName) method
                worldService.deleteWorld(selectedWorldName);
                refreshWorldList();
            }
            return false;
        });

        backButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                // Go back to ModeSelectionScreen (depends on your screen management logic)
            }
            return false;
        });
    }

    private Table createWorldEntry(final String worldName) {
        Table entry = new Table(skin);
        entry.add(new Label(worldName, skin)).expandX().fillX().pad(5);
        entry.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                selectedWorldName = worldName;
            }
            return false;
        });
        return entry;
    }

    private void refreshWorldList() {
        worldListTable.clear();
        List<String> worldNames = worldService.getAvailableWorlds();
        for (String worldName : worldNames) {
            Table entry = createWorldEntry(worldName);
            worldListTable.add(entry).expandX().fillX().row();
        }
        selectedWorldName = null;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Return to ModeSelectionScreen
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { audioService.stopMenuMusic(); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}

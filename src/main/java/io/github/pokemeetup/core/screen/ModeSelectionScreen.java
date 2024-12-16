package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import io.github.pokemeetup.audio.service.AudioService;
import org.springframework.stereotype.Component;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

@Component
public class ModeSelectionScreen implements Screen {
    private final AudioService audioService;
    private Stage stage;
    private Skin skin;

    public ModeSelectionScreen(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public void show() {
        audioService.playMenuMusic();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Setup UI (using a loaded skin from your UIService)
        skin = new Skin(Gdx.files.internal("assets/Skins/uiskin.json"));

        Table root = new Table(skin);
        root.setFillParent(true);

        Label titleLabel = new Label("MineMon", skin, "default");
        Label versionLabel = new Label("Version 1.0", skin);

        TextButton singlePlayerButton = new TextButton("Single Player", skin);
        TextButton multiplayerButton = new TextButton("Multiplayer", skin);
        TextButton exitButton = new TextButton("Exit Game", skin);

        root.add(titleLabel).center().row();
        root.add(versionLabel).center().padBottom(30).row();
        root.add(singlePlayerButton).expandX().fillX().pad(10).row();
        root.add(multiplayerButton).expandX().fillX().pad(10).row();
        root.add(exitButton).expandX().fillX().pad(10).row();

        stage.addActor(root);

        singlePlayerButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                // Navigate to WorldSelectionScreen
                // Use a ScreenManager or context to switch screens
                // e.g., YourAppContext.getScreenManager().setScreen(WorldSelectionScreen.class);
            }
            return false;
        });

        multiplayerButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                // Navigate to MultiplayerScreen
                // e.g., YourAppContext.getScreenManager().setScreen(MultiplayerScreen.class);
            }
            return false;
        });

        exitButton.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                Gdx.app.exit();
            }
            return false;
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    // Other methods required by Screen
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { audioService.stopMenuMusic(); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}

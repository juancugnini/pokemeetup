package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.service.ScreenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModeSelectionScreen implements Screen {

    private final AudioService audioService;
    private final ScreenManager screenManager;

    private Stage stage;
    private Skin skin;

    @Autowired
    public ModeSelectionScreen(AudioService audioService, ScreenManager screenManager) {
        this.audioService = audioService;
        this.screenManager = screenManager;
    }

    @Override
    public void show() {
        audioService.playMenuMusic();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("assets/Skins/uiskin.json"));

        Table root = new Table(skin);
        root.setFillParent(true);

        Label titleLabel = new Label("MineMon", skin, "default");
        Label versionLabel = new Label("Version 1.0", skin);

        TextButton singlePlayerButton = new TextButton("Single Player", skin);
        TextButton multiplayerButton = new TextButton("Multiplayer", skin);
        TextButton exitButton = new TextButton("Exit Game", skin);

        // Layout
        root.add(titleLabel).center().row();
        root.add(versionLabel).center().padBottom(30).row();
        root.add(singlePlayerButton).expandX().fillX().pad(10).row();
        root.add(multiplayerButton).expandX().fillX().pad(10).row();
        root.add(exitButton).expandX().fillX().pad(10).row();

        stage.addActor(root);

        // Use ClickListener to handle button clicks
        singlePlayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screenManager.showScreen(WorldSelectionScreen.class);
            }
        });

        multiplayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screenManager.showScreen(MultiplayerScreen.class);
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1); // Ensure background is cleared
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        audioService.stopMenuMusic();
    }
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}

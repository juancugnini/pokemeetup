package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.service.ScreenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

@Component
public class MultiplayerScreen implements Screen {

    private final AudioService audioService;
    private final ScreenManager screenManager;
    private Stage stage;
    private Skin skin;

    @Autowired
    public MultiplayerScreen(AudioService audioService, ScreenManager screenManager) {
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

        Label titleLabel = new Label("Multiplayer Server Setup", skin, "default");
        root.add(titleLabel).pad(20).row();

        TextField motdField = new TextField("", skin);
        motdField.setMessageText("MOTD (Message of the Day)");

        TextField ipField = new TextField("127.0.0.1", skin);
        ipField.setMessageText("Server IP");

        TextField portField = new TextField("54555", skin);
        portField.setMessageText("TCP Port");

        TextField udpField = new TextField("54777", skin);
        udpField.setMessageText("UDP Port");

        TextField maxPlayersField = new TextField("20", skin);
        maxPlayersField.setMessageText("Max Players");

        TextField iconPathField = new TextField("", skin);
        iconPathField.setMessageText("Server Icon Path (optional)");

        root.add(new Label("MOTD:", skin)).left();
        root.add(motdField).expandX().fillX().row();

        root.add(new Label("Server IP:", skin)).left();
        root.add(ipField).expandX().fillX().row();

        root.add(new Label("TCP Port:", skin)).left();
        root.add(portField).expandX().fillX().row();

        root.add(new Label("UDP Port:", skin)).left();
        root.add(udpField).expandX().fillX().row();

        root.add(new Label("Max Players:", skin)).left();
        root.add(maxPlayersField).expandX().fillX().row();

        root.add(new Label("Server Icon:", skin)).left();
        root.add(iconPathField).expandX().fillX().row();

        TextButton saveButton = new TextButton("Save", skin);
        TextButton backButton = new TextButton("Back", skin);

        root.add(saveButton).pad(10);
        root.add(backButton).pad(10);

        stage.addActor(root);

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Future: save these configs via a MultiplayerService
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screenManager.goBack();
            }
        });
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            screenManager.goBack();
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

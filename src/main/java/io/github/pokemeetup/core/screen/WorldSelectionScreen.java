package io.github.pokemeetup.core.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.service.ScreenManager;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.model.WorldData;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class WorldSelectionScreen implements Screen {
    private final AudioService audioService;
    private final WorldService worldService;
    private final ScreenManager screenManager;

    @Value("${world.saveDir:assets/save/worlds/}")
    private String saveDir;

    private Stage stage;
    private Skin skin;
    private Table worldListTable;
    private String selectedWorldName;

    @Autowired
    public WorldSelectionScreen(AudioService audioService,
                                WorldService worldService,
                                PlayerService playerService,
                                ScreenManager screenManager) {
        this.audioService = audioService;
        this.worldService = worldService;
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

        Label title = new Label("Select World", skin);
        root.add(title).pad(10).row();

        refreshWorldListUI(root);

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

        createButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showCreateWorldDialog();
            }
        });

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedWorldName != null) {
                    worldService.loadWorld(selectedWorldName);
                    screenManager.showScreen(GameScreen.class);
                }
            }
        });

        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedWorldName != null) {
                    showDeleteConfirmDialog();
                }
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screenManager.goBack();
            }
        });
    }

    private void showDeleteConfirmDialog() {
        if (selectedWorldName == null) return;
        Dialog dialog = new Dialog("Delete World", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    // User confirmed deletion
                    worldService.deleteWorld(selectedWorldName);
                    refreshWorldList();
                }
            }
        };
        dialog.text("Are you sure you want to delete world '" + selectedWorldName + "'?\nThis action cannot be undone!");
        dialog.button("Delete", true);
        dialog.button("Cancel", false);
        dialog.show(stage);
    }

    private Table createWorldEntry(final String worldName) {
        Table entry = new Table(skin);
        entry.setBackground(skin.newDrawable("white", 0.1f,0.1f,0.1f,1f));

        WorldData meta = loadWorldMetadata(worldName);

        FileHandle iconFile = Gdx.files.local(saveDir + worldName + "/icon.png");
        Image icon;
        if (iconFile.exists()) {
            Texture tex = new Texture(iconFile);
            icon = new Image(new TextureRegion(tex));
        } else {
            // fallback to a default icon if missing
            FileHandle fallbackFile = Gdx.files.internal("assets/icons/default_world_icon.png");
            Texture fallback = new Texture(fallbackFile);
            icon = new Image(new TextureRegion(fallback));
        }

        icon.setScaling(Scaling.fit);
        entry.add(icon).size(64,64).pad(10);

        Table infoTable = new Table(skin);
        Label nameLabel = new Label(worldName, skin);
        long createdDate = meta.getCreatedDate();
        long lastPlayed = meta.getLastPlayed();
        long playedTime = meta.getPlayedTime();

        Label createdLabel = new Label("Created: " + formatDate(createdDate), skin);
        Label lastPlayedLabel = new Label("Last Played: " + formatDate(lastPlayed), skin);
        Label playedTimeLabel = new Label("Played Time: " + formatPlayedTime(playedTime), skin);

        infoTable.add(nameLabel).row();
        infoTable.add(createdLabel).left().row();
        infoTable.add(lastPlayedLabel).left().row();
        infoTable.add(playedTimeLabel).left().row();

        entry.add(infoTable).expand().fill().pad(10);

        entry.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedWorldName = worldName;
                // highlight this entry
                for (Actor actor : worldListTable.getChildren()) {
                    if (actor instanceof Table t) {
                        t.setBackground(skin.newDrawable("white", 0.1f,0.1f,0.1f,1f));
                    }
                }
                entry.setBackground(skin.newDrawable("white", 0.3f,0.6f,1f,1f));
            }
        });
        return entry;
    }

    private WorldData loadWorldMetadata(String worldName) {
        FileHandle worldFile = Gdx.files.local(saveDir + worldName + "/" + worldName + ".json");
        if (!worldFile.exists()) {
            WorldData wd = new WorldData();
            wd.setWorldName(worldName);
            wd.setSeed(0);
            wd.setCreatedDate(0);
            wd.setLastPlayed(0);
            wd.setPlayedTime(0);
            return wd;
        }
        Json json = new Json();
        return json.fromJson(WorldData.class, worldFile.readString());
    }

    private String formatPlayedTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "Never";
        return new SimpleDateFormat("MMM d, yyyy HH:mm").format(new Date(timestamp));
    }

    private void refreshWorldListUI(Table root) {
        List<String> worldNames = worldService.getAvailableWorlds();
        worldListTable = new Table(skin);

        for (String worldName : worldNames) {
            Table entry = createWorldEntry(worldName);
            worldListTable.add(entry).expandX().fillX().row();
        }

        ScrollPane scrollPane = new ScrollPane(worldListTable, skin);
        root.add(scrollPane).expand().fill().row();
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

    private void showCreateWorldDialog() {
        Dialog dialog = new Dialog("Create New World", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    TextField nameField = findActor("worldNameField");
                    TextField seedField = findActor("seedField");

                    String worldName = nameField.getText().trim();
                    if (worldName.isEmpty()) {
                        showErrorDialog("World name cannot be empty");
                        return;
                    }

                    long seed;
                    String seedText = seedField.getText().trim();
                    if (seedText.isEmpty()) {
                        seed = System.currentTimeMillis();
                    } else {
                        try {
                            seed = Long.parseLong(seedText);
                        } catch (NumberFormatException e) {
                            showErrorDialog("Seed must be a valid number");
                            return;
                        }
                    }

                    boolean success = worldService.createWorld(worldName, seed);
                    if (!success) {
                        showErrorDialog("Failed to create the world. The name might already exist or another error occurred.");
                        return;
                    }

                    // Generate a proper world thumbnail after creation
                    worldService.generateWorldThumbnail(worldName);

                    refreshWorldList();
                    selectedWorldName = worldName;
                }
            }
        };

        dialog.getContentTable().pad(10);

        dialog.getContentTable().add(new Label("World Name:", skin)).left().row();
        TextField nameField = new TextField("", skin);
        nameField.setName("worldNameField");
        nameField.setMessageText("Enter world name");
        dialog.getContentTable().add(nameField).width(300).padBottom(10).row();

        dialog.getContentTable().add(new Label("Seed (optional):", skin)).left().row();
        TextField seedField = new TextField("", skin);
        seedField.setName("seedField");
        seedField.setMessageText("Enter seed or leave empty");
        dialog.getContentTable().add(seedField).width(300).padBottom(10).row();

        dialog.button("Create", true);
        dialog.button("Cancel", false);
        dialog.key(Input.Keys.ENTER, true);
        dialog.key(Input.Keys.ESCAPE, false);

        dialog.show(stage);
    }

    private void showErrorDialog(String message) {
        Dialog errorDialog = new Dialog("Error", skin);
        errorDialog.text(message);
        errorDialog.button("OK");
        errorDialog.show(stage);
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
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}

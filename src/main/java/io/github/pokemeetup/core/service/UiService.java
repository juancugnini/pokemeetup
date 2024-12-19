package io.github.pokemeetup.core.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.pokemeetup.core.ui.DialogFactory;
import io.github.pokemeetup.core.ui.StyleFactory;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class UiService {
    private static final String UI_SKIN_PATH = "assets/Skins/uiskin.json";
    private Skin skin;
    private DialogFactory dialogFactory;
    private StyleFactory styleFactory;

    public void initialize() {
        if (skin != null) {
            return;
        }

        skin = new Skin(Gdx.files.internal(UI_SKIN_PATH));
        dialogFactory = new DialogFactory(skin);
        styleFactory = new StyleFactory();
    }

    public void dispose() {
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
    }
}
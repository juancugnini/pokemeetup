package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.screen.ModeSelectionScreen;
import io.github.pokemeetup.core.service.ScreenManager;
import io.github.pokemeetup.core.service.SettingsService;
import io.github.pokemeetup.core.service.UiService;
import io.github.pokemeetup.multiplayer.service.impl.ServerConnectionServiceImpl;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.world.biome.service.BiomeService;
import io.github.pokemeetup.world.model.WorldRenderer;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import io.github.pokemeetup.world.service.impl.ObjectTextureManager;
import org.springframework.stereotype.Component;

@Component
public class GdxGame extends Game {

    @Override
    public void create() {

        SettingsService settingsService = PokemeetupApplication.getSpringContext().getBean(SettingsService.class);
        settingsService.initialize();

        UiService uiService = PokemeetupApplication.getSpringContext().getBean(UiService.class);
        uiService.initialize();

        TileManager tileManager = PokemeetupApplication.getSpringContext().getBean(TileManager.class);
        tileManager.initIfNeeded();

        WorldObjectManager worldObjectManager = PokemeetupApplication.getSpringContext().getBean(WorldObjectManager.class);
        worldObjectManager.initialize();

        WorldService worldService = PokemeetupApplication.getSpringContext().getBean(WorldService.class);
        worldService.initIfNeeded();
        ObjectTextureManager objectTextureManager = PokemeetupApplication.getSpringContext().getBean(ObjectTextureManager.class);
        objectTextureManager.initializeIfNeeded();
        WorldRenderer worldRenderer = PokemeetupApplication.getSpringContext().getBean(WorldRenderer.class);

        worldRenderer.initialize();
        PlayerAnimationService playerAnimationService =
                PokemeetupApplication.getSpringContext().getBean(PlayerAnimationService.class);
        playerAnimationService.initAnimationsIfNeeded();

        AudioService audioService = PokemeetupApplication.getSpringContext().getBean(AudioService.class);
        audioService.initAudio();
        BiomeService biomeService = PokemeetupApplication.getSpringContext().getBean(BiomeService.class);
        biomeService.init();

        Gdx.graphics.setVSync(settingsService.getVSync());


        ScreenManager screenManager = PokemeetupApplication.getSpringContext().getBean(ScreenManager.class);
        screenManager.showScreen(ModeSelectionScreen.class);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        UiService uiService = PokemeetupApplication.getSpringContext().getBean(UiService.class);
        uiService.dispose();

        ObjectTextureManager objectTextureManager = PokemeetupApplication.getSpringContext().getBean(ObjectTextureManager.class);
        objectTextureManager.disposeTextures();

        super.dispose();
    }

}

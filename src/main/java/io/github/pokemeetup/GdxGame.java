package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.screen.ModeSelectionScreen;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.stereotype.Component;

@Component
public class GdxGame extends Game {
    @Override
    public void create() {
        // LibGDX is now initialized
        TileManager tileManager = PokemeetupApplication.getSpringContext().getBean(TileManager.class);
        tileManager.initIfNeeded(); // A new method that does Gdx-related loading

        WorldService worldService = PokemeetupApplication.getSpringContext().getBean(WorldService.class);
        worldService.initIfNeeded(); // Another method to load data using Gdx
        PlayerAnimationService playerAnimationService =
                PokemeetupApplication.getSpringContext().getBean(PlayerAnimationService.class);
        playerAnimationService.initAnimationsIfNeeded();

        ModeSelectionScreen modeSelectionScreen = PokemeetupApplication.getSpringContext().getBean(ModeSelectionScreen.class);
        AudioService audioService = PokemeetupApplication.getSpringContext().getBean(AudioService.class);
        audioService.initAudio();

        setScreen(modeSelectionScreen);
    }


    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

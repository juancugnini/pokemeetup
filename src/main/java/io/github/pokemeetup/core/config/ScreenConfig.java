package io.github.pokemeetup.core.config;

import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.screen.GameScreen;
import io.github.pokemeetup.core.screen.ModeSelectionScreen;
import io.github.pokemeetup.core.screen.MultiplayerScreen;
import io.github.pokemeetup.core.screen.WorldSelectionScreen;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScreenConfig {

    @Bean
    public GameScreen gameScreen(
            PlayerService playerService,
            WorldService worldService,
            AudioService audioService,
            TileManager tileManager,
            WorldObjectManager worldObjectManager,
            InputService inputService
    ) {
        return new GameScreen(playerService, worldService, audioService, tileManager, worldObjectManager, inputService);
    }

    @Bean
    public ModeSelectionScreen modeSelectionScreen(
            AudioService audioService
    ) {
        return new ModeSelectionScreen(audioService);
    }

    @Bean
    public WorldSelectionScreen worldSelectionScreen(
            AudioService audioService,
            WorldService worldService,
            PlayerService playerService
    ) {
        return new WorldSelectionScreen(audioService, worldService, playerService);
    }

    @Bean
    public MultiplayerScreen multiplayerScreen(
            AudioService audioService
    ) {
        return new MultiplayerScreen(audioService);
    }
}

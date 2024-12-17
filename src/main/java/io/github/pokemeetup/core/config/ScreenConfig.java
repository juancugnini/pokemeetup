package io.github.pokemeetup.core.config;

import com.badlogic.gdx.Game;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.core.screen.GameScreen;
import io.github.pokemeetup.core.screen.ModeSelectionScreen;
import io.github.pokemeetup.core.screen.MultiplayerScreen;
import io.github.pokemeetup.core.screen.WorldSelectionScreen;
import io.github.pokemeetup.core.service.ScreenManager;
import io.github.pokemeetup.core.service.impl.ScreenManagerImpl;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.service.TileManager;
import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScreenConfig {

    @Bean
    public ScreenManager screenManager(ApplicationContext applicationContext, Game game) {
        return new ScreenManagerImpl(applicationContext, game);
    }

    @Bean
    public ModeSelectionScreen modeSelectionScreen(AudioService audioService, ScreenManager screenManager) {
        return new ModeSelectionScreen(audioService, screenManager);
    }

    @Bean
    public WorldSelectionScreen worldSelectionScreen(AudioService audioService,
                                                     WorldService worldService,
                                                     PlayerService playerService,
                                                     ScreenManager screenManager) {
        return new WorldSelectionScreen(audioService, worldService, playerService, screenManager);
    }

    @Bean
    public MultiplayerScreen multiplayerScreen(AudioService audioService, ScreenManager screenManager) {
        return new MultiplayerScreen(audioService, screenManager);
    }

    @Bean
    public GameScreen gameScreen(PlayerService playerService,
                                 WorldService worldService,
                                 AudioService audioService,
                                 TileManager tileManager,
                                 WorldObjectManager worldObjectManager,
                                 InputService inputService,
                                 ScreenManager screenManager) {
        return new GameScreen(playerService, worldService, audioService, tileManager, worldObjectManager, inputService, screenManager);
    }
}

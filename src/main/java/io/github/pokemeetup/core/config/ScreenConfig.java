package io.github.pokemeetup.core.config;

import com.badlogic.gdx.Game;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.chat.service.ChatService;
import io.github.pokemeetup.core.screen.*;
import io.github.pokemeetup.core.service.*;
import io.github.pokemeetup.core.service.impl.ScreenManagerImpl;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.multiplayer.service.ServerConnectionService;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.biome.service.BiomeService;
import io.github.pokemeetup.world.model.WorldRenderer;
import io.github.pokemeetup.world.service.ChunkLoaderService;
import io.github.pokemeetup.world.service.ChunkPreloaderService;
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
    public ChunkPreloaderService chunkPreloaderService(WorldService worldService) {
        return new ChunkPreloaderService(worldService);
    }

    @Bean
    public WorldSelectionScreen worldSelectionScreen(
            AudioService audioService,
            WorldService worldService,
            PlayerService playerService,
            ScreenManager screenManager) {
        return new WorldSelectionScreen(
                audioService,
                worldService,
                screenManager
        );
    }

    @Bean
    public LoginScreen loginScreen(
            AudioService audioService,
            ScreenManager screenManager,
            ServerConnectionService serverConnectionService,
            MultiplayerClient multiplayerClient,
            UiService uiService) {
        return new LoginScreen(
                audioService,
                screenManager,
                serverConnectionService,
                multiplayerClient,
                uiService
        );
    }

    @Bean
    public GameScreen gameScreen(
            PlayerService playerService,
            WorldService worldService,
            AudioService audioService,
            InputService inputService,
            ScreenManager screenManager,
            ChatService chatService,
            BiomeService biomeService,
            WorldRenderer worldRenderer,
            ChunkLoaderService chunkLoaderService,
            ChunkPreloaderService chunkPreloaderService, PlayerAnimationService animationService, MultiplayerClient client) {
        return new GameScreen(
                playerService,
                worldService,
                audioService,
                inputService,
                screenManager,
                chatService,
                biomeService,
                worldRenderer,
                chunkLoaderService,
                chunkPreloaderService, animationService,
                client);
    }

    @Bean
    public ModeSelectionScreen modeSelectionScreen(
            AudioService audioService,
            ScreenManager screenManager,
            GameConfig gameConfig,
            SettingsService settingsService,
            BackgroundService backgroundService) {
        return new ModeSelectionScreen(
                audioService,
                screenManager,
                settingsService,
                backgroundService
        );
    }

    @Bean
    public SettingsScreen settingsScreen(
            ScreenManager screenManager,
            SettingsService settingsService,
            BackgroundService backgroundService) {
        return new SettingsScreen(
                screenManager,
                settingsService,
                backgroundService
        );
    }

    @Bean
    public BackgroundService backgroundService() {
        return new BackgroundService();
    }
}

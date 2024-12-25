package io.github.pokemeetup.multiplayer.config;

import io.github.pokemeetup.core.service.FileAccessService;
import io.github.pokemeetup.core.service.impl.LocalFileAccessService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.world.service.WorldService;
import io.github.pokemeetup.multiplayer.service.MultiplayerService;
import io.github.pokemeetup.multiplayer.service.impl.MultiplayerServiceImpl;
import io.github.pokemeetup.multiplayer.service.MultiplayerServer;
import io.github.pokemeetup.multiplayer.service.impl.MultiplayerServerImpl;
import io.github.pokemeetup.multiplayer.service.ServerConnectionService;
import io.github.pokemeetup.multiplayer.service.impl.ServerConnectionServiceImpl;
import io.github.pokemeetup.event.EventBus;
import io.github.pokemeetup.multiplayer.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiplayerConfig {

    @Bean
    public FileAccessService fileAccessService() {
        return new LocalFileAccessService();
    }

    @Bean
    public ServerConnectionService serverConnectionService(FileAccessService fileAccessService) {
        return new ServerConnectionServiceImpl(fileAccessService);
    }

    @Bean
    public MultiplayerService multiplayerService(WorldService worldService, PlayerService playerService) {
        return new MultiplayerServiceImpl(worldService);
    }

    @Bean
    public MultiplayerServer multiplayerServer(MultiplayerService multiplayerService, EventBus eventBus, AuthService authService) {
        return new MultiplayerServerImpl(multiplayerService, eventBus, authService);
    }

}

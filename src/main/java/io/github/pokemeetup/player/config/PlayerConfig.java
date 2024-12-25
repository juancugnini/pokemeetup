package io.github.pokemeetup.player.config;

import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.player.service.impl.PlayerServiceImpl;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(PlayerProperties.class)
public class PlayerConfig {

    private final InputService inputService;
    private final PlayerProperties playerProperties;
    private final WorldService worldService;

    @Autowired
    public PlayerConfig(InputService inputService, PlayerProperties playerProperties, WorldService worldService) {
        this.inputService = inputService;
        this.playerProperties = playerProperties;
        this.worldService = worldService;

    }

    
    @Bean
    public PlayerService playerService(
            PlayerAnimationService animationService
    ) {
        return new PlayerServiceImpl(animationService, inputService, playerProperties, worldService);
    }
}

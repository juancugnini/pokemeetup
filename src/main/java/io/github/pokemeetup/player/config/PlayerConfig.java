package io.github.pokemeetup.player.config;

import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.player.service.impl.PlayerAnimationServiceImpl;
import io.github.pokemeetup.player.service.impl.PlayerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayerConfig {

    public PlayerAnimationService playerAnimationService() {
        return new PlayerAnimationServiceImpl();
    }

    @Bean
    public PlayerService playerService(PlayerAnimationService animationService) {
        return new PlayerServiceImpl(animationService);
    }
}

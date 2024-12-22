package io.github.pokemeetup.world.config;

import io.github.pokemeetup.world.service.WorldObjectManager;
import io.github.pokemeetup.world.service.impl.server.ServerWorldObjectManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class WorldObjectConfig {

    @Bean
    @Profile("server")
    public WorldObjectManager serverWorldObjectManager() {
        return new ServerWorldObjectManagerImpl();
    }
}

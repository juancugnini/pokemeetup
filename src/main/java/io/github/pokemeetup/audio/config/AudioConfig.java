package io.github.pokemeetup.audio.config;

import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.audio.service.impl.AudioServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AudioConfig {

    @Bean
    public AudioService audioService() {
        return new AudioServiceImpl();
    }
}

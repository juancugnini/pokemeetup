package io.github.pokemeetup.audio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@Data
@ConfigurationProperties(prefix = "sounds")
public class SoundConfig {
    private float masterVolume;
    private float musicVolume;
    private boolean musicEnabled;
    private float musicFadeDuration;
    private float fadeOutDuration;
    private Map<String, String> weather;
    private Map<String, String> effects;
    private Map<String, String> ambient;
    private List<String> menu;
    private Map<String, List<String>> biomes;
}

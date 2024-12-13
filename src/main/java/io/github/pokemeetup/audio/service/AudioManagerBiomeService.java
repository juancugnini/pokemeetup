package io.github.pokemeetup.audio.service;

import com.badlogic.gdx.audio.Music;
import io.github.pokemeetup.audio.config.SoundConfig;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AudioManagerBiomeService extends AudioManagerAbstractService {

    @Autowired
    SoundConfig soundConfig;
    private Music currentMusic;
    private final Map<String, List<Music>> biomeMusic;

    public AudioManagerBiomeService() {
        biomeMusic = new HashMap<>();
        soundConfig.getBiomes().forEach((name, sounds) -> {
            this.biomeMusic.put(name, loadSoundsFromList(sounds));
        });
    }

    @Override
    public void playAudio() {
        if (soundConfig.isMusicEnabled() && (currentMusic == null || !currentMusic.isPlaying())) {

        }
    }

    @Override
    public void increaseVolume() {

    }

    @Override
    public void decreaseVolume() {

    }

    @Override
    public void stopAmbientSound() {

    }

    @PreDestroy
    @Override
    public void dispose() {

    }
}

package io.github.pokemeetup.audio.service;

import io.github.pokemeetup.audio.config.SoundConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AudioManagerWorldService implements AudioManagerService {

    @Autowired
    SoundConfig soundConfig;

    @Override
    public void playAudio() {

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

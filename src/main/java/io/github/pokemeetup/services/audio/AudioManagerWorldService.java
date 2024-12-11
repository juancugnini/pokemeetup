package io.github.pokemeetup.services.audio;

import io.github.pokemeetup.config.SoundConfig;
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

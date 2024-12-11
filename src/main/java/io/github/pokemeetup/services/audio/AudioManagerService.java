package io.github.pokemeetup.services.audio;


import jakarta.annotation.PreDestroy;

public interface AudioManagerService {
    void playAudio();
    void increaseVolume();
    void decreaseVolume();
    void stopAmbientSound();
    void dispose();
}

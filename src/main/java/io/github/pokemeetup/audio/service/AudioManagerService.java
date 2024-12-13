package io.github.pokemeetup.audio.service;


import jakarta.annotation.PreDestroy;

public interface AudioManagerService {
    void playAudio();
    void increaseVolume();
    void decreaseVolume();
    void stopAmbientSound();
    void dispose();
}

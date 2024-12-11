package io.github.pokemeetup.services.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import io.github.pokemeetup.config.SoundConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class AudioManagerAbstractService implements AudioManagerService {

    @Autowired
    public SoundConfig soundConfig;

    public List<Music> loadSoundsFromList(List<String> paths) {
        return paths.stream().map(path -> {
            try {
                Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
                music.setVolume(soundConfig.getMusicVolume() * soundConfig.getMasterVolume());
                return music;
            } catch (Exception e) {
                log.error("Error loading sound: {} {}", path, e.getMessage());
                e.printStackTrace();
                return null;
            }
        }).toList();
    }
    @Override
    public void increaseVolume() {

    }

    @Override
    public void decreaseVolume() {

    }
}

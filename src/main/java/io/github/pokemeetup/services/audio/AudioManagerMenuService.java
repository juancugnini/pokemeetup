package io.github.pokemeetup.services.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import io.github.pokemeetup.config.SoundConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AudioManagerMenuService extends AudioManagerAbstractService {

    private Music currentMusic;
    private final List<Music> menuMusic;
    private boolean isFadingInMusic = false;
    private float fadeInMusicTimer;

    public AudioManagerMenuService() {
        menuMusic = loadSoundsFromList(soundConfig.getMenu());
    }

    @Override
    public void playAudio() {
        if (soundConfig.isMusicEnabled() && (currentMusic == null || !currentMusic.isPlaying())) {
            stopAmbientSound();
            int index = MathUtils.random(menuMusic.size() - 1);
            currentMusic = menuMusic.get(index);
            currentMusic.setVolume(0f);
            currentMusic.setLooping(false);
            currentMusic.play();
            isFadingInMusic = true;
            fadeInMusicTimer = soundConfig.getMusicFadeDuration();
            setMusicCompletionListenerForMenu();
        }
    }



    @Override
    public void stopAmbientSound() {

    }

    @Override
    public void dispose() {

    }

    private void setMusicCompletionListenerForMenu() {
        if (currentMusic != null) {
            currentMusic.setOnCompletionListener(music -> {
                playAudio();
            });
        }
    }
}

package io.github.pokemeetup.audio.service.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import io.github.pokemeetup.audio.model.SoundEffect;
import io.github.pokemeetup.audio.service.AudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Primary
public class AudioServiceImpl implements AudioService {
    private static final Logger logger = LoggerFactory.getLogger(AudioServiceImpl.class);

    private final Map<SoundEffect, Sound> sounds = new EnumMap<>(SoundEffect.class);

    private List<Music> menuMusicList;
    private Music currentMusic;

    private final float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float soundVolume = 1.0f;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;

    private static final float MUSIC_FADE_DURATION = 2.0f;
    private static final float FADE_OUT_DURATION = 2f;

    private boolean isFadingOutMusic = false;
    private float fadeOutMusicTimer = 0f;
    private boolean isFadingInMusic = false;
    private float fadeInMusicTimer = 0f;

    public AudioServiceImpl() {
        // Don't initialize audio here!
        // Gdx is not yet initialized.
    }

    @Override
    public void initAudio() {
        // This method is called after Gdx is ready
        initializeAudio();
    }

    private void initializeAudio() {
        // Load sounds
        for (SoundEffect effect : SoundEffect.values()) {
            try {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal("assets/" + effect.getPath()));
                sounds.put(effect, sound);
            } catch (Exception e) {
                logger.error("Failed to load sound: {}", effect.getPath(), e);
            }
        }

        menuMusicList = new ArrayList<>();
        loadMenuMusic(Arrays.asList(
                "music/Menu-Music-1.mp3",
                "music/Menu-Music-2.mp3",
                "music/Menu-Music-0.mp3",
                "music/Menu-Music-3.mp3",
                "music/Menu-Music-4.mp3"
        ));
    }

    private void loadMenuMusic(List<String> paths) {
        for (String path : paths) {
            try {
                Music music = Gdx.audio.newMusic(Gdx.files.internal("assets/" + path));
                music.setVolume(musicVolume * masterVolume);
                menuMusicList.add(music);
            } catch (Exception e) {
                logger.error("Failed to load menu music: {}", path, e);
            }
        }
    }

    @Override
    public void playMenuMusic() {
        if (!musicEnabled || menuMusicList == null || menuMusicList.isEmpty()) {
            return;
        }

        if (currentMusic == null || !currentMusic.isPlaying()) {
            stopCurrentMusic();
            int index = MathUtils.random(menuMusicList.size() - 1);
            currentMusic = menuMusicList.get(index);
            currentMusic.setVolume(0f);
            currentMusic.setLooping(false);
            currentMusic.play();
            isFadingInMusic = true;
            fadeInMusicTimer = MUSIC_FADE_DURATION;
            setMusicCompletionListenerForMenu();
        }
    }

    private void setMusicCompletionListenerForMenu() {
        if (currentMusic != null) {
            currentMusic.setOnCompletionListener(music -> playMenuMusic());
        }
    }

    @Override
    public void stopMenuMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOutMusic = true;
            fadeOutMusicTimer = MUSIC_FADE_DURATION;
        }
    }

    @Override
    public void fadeOutMenuMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOutMusic = true;
            fadeOutMusicTimer = FADE_OUT_DURATION;
        }
    }

    @Override
    public void update(float delta) {
        if (isFadingInMusic && currentMusic != null) {
            fadeInMusicTimer -= delta;
            float progress = 1 - Math.max(0, fadeInMusicTimer / MUSIC_FADE_DURATION);
            float volume = progress * musicVolume * masterVolume;
            currentMusic.setVolume(volume);

            if (fadeInMusicTimer <= 0) {
                isFadingInMusic = false;
                currentMusic.setVolume(musicVolume * masterVolume);
            }
        }

        if (isFadingOutMusic && currentMusic != null) {
            fadeOutMusicTimer -= delta;
            float volume = Math.max(0, (fadeOutMusicTimer / MUSIC_FADE_DURATION) * musicVolume * masterVolume);
            currentMusic.setVolume(volume);

            if (fadeOutMusicTimer <= 0) {
                currentMusic.stop();
                isFadingOutMusic = false;
                currentMusic = null;
            }
        }
    }

    @Override
    public void playSound(SoundEffect effect) {
        if (!soundEnabled) return;
        Sound sound = sounds.get(effect);
        if (sound != null) {
            sound.play(soundVolume * masterVolume);
        }
    }

    @Override
    public float getMusicVolume() {
        return musicVolume;
    }

    @Override
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume * masterVolume);
        }
    }

    @Override
    public float getSoundVolume() {
        return soundVolume;
    }

    @Override
    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    @Override
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    @Override
    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
        if (currentMusic != null) {
            if (musicEnabled) {
                if (!currentMusic.isPlaying()) {
                    currentMusic.play();
                }
                currentMusic.setVolume(musicVolume * masterVolume);
            } else {
                currentMusic.pause();
            }
        }
    }

    @Override
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    @Override
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    private void stopCurrentMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    @Override
    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();

        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }

        if (menuMusicList != null) {
            for (Music music : menuMusicList) {
                music.dispose();
            }
            menuMusicList.clear();
        }
    }
}

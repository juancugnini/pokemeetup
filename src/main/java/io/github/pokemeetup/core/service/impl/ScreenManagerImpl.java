package io.github.pokemeetup.core.service.impl;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import io.github.pokemeetup.core.service.ScreenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
public class ScreenManagerImpl implements ScreenManager {
    private final ApplicationContext applicationContext;
    private final Game game;
    private final Stack<Class<? extends Screen>> screenHistory = new Stack<>();

    @Autowired
    public ScreenManagerImpl(ApplicationContext applicationContext, Game game) {
        this.applicationContext = applicationContext;
        this.game = game;
    }

    @Override
    public void showScreen(Class<? extends Screen> screenClass) {
        screenHistory.push(screenClass);
        Screen newScreen = applicationContext.getBean(screenClass);
        Gdx.app.postRunnable(() -> game.setScreen(newScreen));
    }

    @Override
    public void goBack() {
        Gdx.app.postRunnable(() -> {
            if (screenHistory.size() <= 1) return;
            screenHistory.pop();
            Class<? extends Screen> previous = screenHistory.peek();
            Screen newScreen = applicationContext.getBean(previous);
            game.setScreen(newScreen);
        });
    }
}
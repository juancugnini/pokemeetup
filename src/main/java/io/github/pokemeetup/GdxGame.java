package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import io.github.pokemeetup.core.screen.GameScreen;
import io.github.pokemeetup.core.screen.ModeSelectionScreen;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/**
 * Main game class that integrates libGDX with Spring Boot.
 */
public class GdxGame extends Game {
    private ApplicationContext springContext;

    /**
     * Initializes the game by setting up Spring context and screen.
     */
    @Override
    public void create() {
        // Initialize Spring context
        springContext = SpringApplication.run(PokemeetupApplication.class);

        // Retrieve GameScreen from Spring context
        GameScreen gameScreen = springContext.getBean(GameScreen.class);
        ModeSelectionScreen modeSelectionScreen = springContext.getBean(ModeSelectionScreen.class);
        // Set the active screen
        setScreen(modeSelectionScreen);
    }

    @Override
    public void render() {
        super.render(); // Delegate to active screen
    }

    @Override
    public void dispose() {
        super.dispose(); // Dispose of active screen and other resources
    }
}

package io.github.pokemeetup;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.pokemeetup.screens.GameScreen;
import io.github.pokemeetup.world.service.TileManager;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import io.github.pokemeetup.audio.service.AudioService;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.world.service.WorldService;

public class GdxGame extends Game {
    private ApplicationContext springContext;

    private AudioService audioService;
    private PlayerAnimationService playerAnimationService;
    private PlayerService playerService;
    private InputService inputService;
    private WorldService worldService;
    private TileManager tileManager;

    @Override
    public void create() {
        
        springContext = SpringApplication.run(PokemeetupApplication.class);

        
        audioService = springContext.getBean(AudioService.class);
        playerAnimationService = springContext.getBean(PlayerAnimationService.class);
        playerService = springContext.getBean(PlayerService.class);
        inputService = springContext.getBean(InputService.class);
        worldService = springContext.getBean(WorldService.class);
        tileManager = springContext.getBean(TileManager.class);
        setScreen(new GameScreen(playerService,worldService,audioService,tileManager));
    }

    @Override
    public void render() {
        super.render();
        
    }

    @Override
    public void dispose() {
        super.dispose();
        
    }
}

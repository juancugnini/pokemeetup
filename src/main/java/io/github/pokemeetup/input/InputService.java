package io.github.pokemeetup.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.pokemeetup.player.model.PlayerDirection;
import io.github.pokemeetup.player.service.PlayerService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class InputService {

    private final PlayerService playerService;
    private final InputConfiguration config;

    private boolean running = false;

    public InputService(PlayerService playerService, InputConfiguration config) {
        this.playerService = playerService;
        this.config = config;
    }

    public void update(float delta) {
        running = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        playerService.setRunning(running);

        
        boolean moved = false;
        if (isKeyPressedForDirection(PlayerDirection.UP)) {
            playerService.move(PlayerDirection.UP);
            moved = true;
        }
        if (isKeyPressedForDirection(PlayerDirection.DOWN)) {
            playerService.move(PlayerDirection.DOWN);
            moved = true;
        }
        if (isKeyPressedForDirection(PlayerDirection.LEFT)) {
            playerService.move(PlayerDirection.LEFT);
            moved = true;
        }
        if (isKeyPressedForDirection(PlayerDirection.RIGHT)) {
            playerService.move(PlayerDirection.RIGHT);
            moved = true;
        }

        
    }

    private boolean isKeyPressedForDirection(PlayerDirection direction) {
        
        for (int key : configKeysForDirection(direction)) {
            if (Gdx.input.isKeyPressed(key)) return true;
        }
        return false;
    }

    private int[] configKeysForDirection(PlayerDirection direction) {
        return switch (direction) {
            case UP -> new int[]{Input.Keys.W, Input.Keys.UP};
            case DOWN -> new int[]{Input.Keys.S, Input.Keys.DOWN};
            case LEFT -> new int[]{Input.Keys.A, Input.Keys.LEFT};
            case RIGHT -> new int[]{Input.Keys.D, Input.Keys.RIGHT};
        };
    }
}

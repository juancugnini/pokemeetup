package io.github.pokemeetup.input;

import com.badlogic.gdx.InputAdapter;
import io.github.pokemeetup.player.model.PlayerDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InputService extends InputAdapter {
    private final InputConfiguration inputConfig;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean runPressed;

    public PlayerDirection getCurrentDirection() {
        if (upPressed) return PlayerDirection.UP;
        if (downPressed) return PlayerDirection.DOWN;
        if (leftPressed) return PlayerDirection.LEFT;
        if (rightPressed) return PlayerDirection.RIGHT;
        return null;
    }

    public boolean isRunning() {
        return runPressed;
    }

    @Override
    public boolean keyDown(int keycode) {
        PlayerDirection direction = inputConfig.getDirectionForKey(keycode);
        if (direction != null) {
            switch (direction) {
                case UP:
                    upPressed = true;
                    return true;
                case DOWN:
                    downPressed = true;
                    return true;
                case LEFT:
                    leftPressed = true;
                    return true;
                case RIGHT:
                    rightPressed = true;
                    return true;
            }
        }

        if (keycode == inputConfig.getRunKey()) {
            runPressed = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        PlayerDirection direction = inputConfig.getDirectionForKey(keycode);
        if (direction != null) {
            switch (direction) {
                case UP:
                    upPressed = false;
                    return true;
                case DOWN:
                    downPressed = false;
                    return true;
                case LEFT:
                    leftPressed = false;
                    return true;
                case RIGHT:
                    rightPressed = false;
                    return true;
            }
        }

        if (keycode == inputConfig.getRunKey()) {
            runPressed = false;
            return true;
        }

        return false;
    }
}
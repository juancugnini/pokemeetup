package io.github.pokemeetup.input;

import com.badlogic.gdx.InputAdapter;
import io.github.pokemeetup.player.model.PlayerDirection;
import org.springframework.stereotype.Service;

/**
 * Service to handle player input.
 */
@Service
public class InputService extends InputAdapter {

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean runPressed;

    /**
     * Returns the current direction based on pressed keys.
     * Priority: UP > DOWN > LEFT > RIGHT
     *
     * @return Current PlayerDirection or null if no direction is pressed.
     */
    public PlayerDirection getCurrentDirection() {
        if (upPressed) return PlayerDirection.UP;
        if (downPressed) return PlayerDirection.DOWN;
        if (leftPressed) return PlayerDirection.LEFT;
        if (rightPressed) return PlayerDirection.RIGHT;
        return null;
    }

    /**
     * Checks if the player is currently running.
     *
     * @return True if running, else false.
     */
    public boolean isRunning() {
        return runPressed;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.UP:
                upPressed = true;
                return true;
            case com.badlogic.gdx.Input.Keys.DOWN:
                downPressed = true;
                return true;
            case com.badlogic.gdx.Input.Keys.LEFT:
                leftPressed = true;
                return true;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                rightPressed = true;
                return true;
            case com.badlogic.gdx.Input.Keys.Z:
                runPressed = true;
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.UP:
                upPressed = false;
                return true;
            case com.badlogic.gdx.Input.Keys.DOWN:
                downPressed = false;
                return true;
            case com.badlogic.gdx.Input.Keys.LEFT:
                leftPressed = false;
                return true;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                rightPressed = false;
                return true;
            case com.badlogic.gdx.Input.Keys.Z:
                runPressed = false;
                return true;
            default:
                return false;
        }
    }
}

package io.github.pokemeetup.input;

import com.badlogic.gdx.Input;
import io.github.pokemeetup.player.model.PlayerDirection;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class InputConfiguration {

    private final Map<Integer, PlayerDirection> movementKeys = new HashMap<>();

    public InputConfiguration() {
        
        movementKeys.put(Input.Keys.W, PlayerDirection.UP);
        movementKeys.put(Input.Keys.UP, PlayerDirection.UP);

        movementKeys.put(Input.Keys.S, PlayerDirection.DOWN);
        movementKeys.put(Input.Keys.DOWN, PlayerDirection.DOWN);

        movementKeys.put(Input.Keys.A, PlayerDirection.LEFT);
        movementKeys.put(Input.Keys.LEFT, PlayerDirection.LEFT);

        movementKeys.put(Input.Keys.D, PlayerDirection.RIGHT);
        movementKeys.put(Input.Keys.RIGHT, PlayerDirection.RIGHT);
    }

    public PlayerDirection getDirectionForKey(int keyCode) {
        return movementKeys.get(keyCode);
    }
}

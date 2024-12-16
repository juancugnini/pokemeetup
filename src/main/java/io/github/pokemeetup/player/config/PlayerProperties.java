package io.github.pokemeetup.player.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the player.
 */
@Configuration
@ConfigurationProperties(prefix = "player")
public class PlayerProperties {

    /**
     * The username of the player.
     */
    private String username = "Player";

    /**
     * Duration to walk one tile (in seconds).
     */
    private float walkStepDuration = 0.3f;

    /**
     * Duration to run one tile (in seconds).
     */
    private float runStepDuration = 0.15f;

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getWalkStepDuration() {
        return walkStepDuration;
    }

    public void setWalkStepDuration(float walkStepDuration) {
        this.walkStepDuration = walkStepDuration;
    }

    public float getRunStepDuration() {
        return runStepDuration;
    }

    public void setRunStepDuration(float runStepDuration) {
        this.runStepDuration = runStepDuration;
    }
}

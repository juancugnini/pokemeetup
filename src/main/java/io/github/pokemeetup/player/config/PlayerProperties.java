package io.github.pokemeetup.player.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the player.
 */
@Setter
@Getter
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

}

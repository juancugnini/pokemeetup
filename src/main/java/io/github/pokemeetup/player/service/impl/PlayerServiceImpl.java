package io.github.pokemeetup.player.service.impl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.model.PlayerDirection;
import io.github.pokemeetup.player.model.PlayerModel;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.config.PlayerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerServiceImpl implements PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerModel playerModel;
    private final PlayerAnimationService animationService;
    private final String username;

    // Movement durations (in seconds)
    private final float walkStepDuration;
    private final float runStepDuration;

    private final InputService inputService;


    /**
     * Constructor with dependency injection.
     *
     * @param animationService  Service for handling player animations.
     * @param inputService      Service for handling input states.
     * @param playerProperties  Configuration properties for the player.
     */
    public PlayerServiceImpl(
            PlayerAnimationService animationService,
            InputService inputService,
            PlayerProperties playerProperties
    ) {
        this.playerModel = new PlayerModel(0, 0);
        this.animationService = animationService;
        this.inputService = inputService;
        this.username = playerProperties.getUsername();
        this.walkStepDuration = playerProperties.getWalkStepDuration();
        this.runStepDuration = playerProperties.getRunStepDuration();
        this.playerModel.setRunning(false);
    }

    /**
     * Initiates movement in the specified direction if not already moving.
     *
     * @param direction Direction to move.
     */
    @Override
    public void move(PlayerDirection direction) {
        if (playerModel.isMoving()) {
            logger.debug("Movement already in progress. Ignoring move command.");
            return; // Prevent overlapping movements
        }

        // Update running state based on input
        playerModel.setRunning(inputService.isRunning());

        playerModel.setDirection(direction);

        float currentX = playerModel.getPosition().x;
        float currentY = playerModel.getPosition().y;
        float tileSize = PlayerModel.TILE_SIZE;

        float targetX = currentX;
        float targetY = currentY;

        switch (direction) {
            case UP -> targetY += tileSize;
            case DOWN -> targetY -= tileSize;
            case LEFT -> targetX -= tileSize;
            case RIGHT -> targetX += tileSize;
        }

        playerModel.setStartPosition(currentX, currentY);
        playerModel.setTargetPosition(targetX, targetY);

        float duration = playerModel.isRunning() ? runStepDuration : walkStepDuration;
        playerModel.setMovementDuration(duration);

        playerModel.setStateTime(0f);
        playerModel.setMovementTime(0f);
        playerModel.setMoving(true);

        logger.debug("Initiated movement: Direction={}, Target=({}, {}), Duration={}",
                direction, targetX, targetY, duration);
    }

    /**
     * Updates the player's movement and animation based on delta time.
     *
     * @param delta Time elapsed since the last frame.
     */
    @Override
    public void update(float delta) {
        if (playerModel.isMoving()) {
            // Increment movementTime by delta
            playerModel.setMovementTime(playerModel.getMovementTime() + delta);

            // Calculate progress [0,1]
            float progress = playerModel.getMovementTime() / playerModel.getMovementDuration();

            if (progress >= 1f) {
                progress = 1f;
                playerModel.setMoving(false);
            }

            // Linear interpolation for consistent speed
            float newX = MathUtils.lerp(playerModel.getStartPosition().x, playerModel.getTargetPosition().x, progress);
            float newY = MathUtils.lerp(playerModel.getStartPosition().y, playerModel.getTargetPosition().y, progress);

            playerModel.setPosition(newX, newY);

            if (playerModel.isMoving()) {
                // Update animation state time
                playerModel.setStateTime(playerModel.getStateTime() + delta);
            } else {
                // Reset animation state time
                playerModel.setStateTime(0f);
                logger.debug("Movement completed. New Position: ({}, {})", newX, newY);

                // Check if a direction key is still pressed and initiate next movement
                PlayerDirection currentDirection = inputService.getCurrentDirection();
                if (currentDirection != null) {
                    move(currentDirection);
                }
            }
        } else {
            // Not moving, check if a direction key is pressed and initiate movement
            PlayerDirection currentDirection = inputService.getCurrentDirection();
            if (currentDirection != null) {
                move(currentDirection);
            }
        }
    }

    /**
     * Renders the player on the screen.
     *
     * @param batch SpriteBatch used for rendering.
     */
    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = animationService.getCurrentFrame(
                playerModel.getDirection(),
                playerModel.isMoving(),
                playerModel.isRunning(),
                playerModel.getStateTime()
        );
        batch.draw(frame, playerModel.getPosition().x, playerModel.getPosition().y);
    }

    /**
     * Retrieves the player's data.
     *
     * @return PlayerData object containing username and tile positions.
     */
    @Override
    public PlayerData getPlayerData() {
        float tileX = playerModel.getPosition().x / PlayerModel.TILE_SIZE;
        float tileY = playerModel.getPosition().y / PlayerModel.TILE_SIZE;
        return new PlayerData(username, tileX, tileY);
    }

    /**
     * Sets whether the player is running.
     *
     * @param running True to enable running, false to disable.
     */
    @Override
    public void setRunning(boolean running) {
        playerModel.setRunning(running);
        logger.debug("Set running to {}", running);
    }

    /**
     * Sets the player's position directly.
     *
     * @param tileX X-coordinate in tiles.
     * @param tileY Y-coordinate in tiles.
     */
    @Override
    public void setPosition(int tileX, int tileY) {
        float x = tileX * PlayerModel.TILE_SIZE;
        float y = tileY * PlayerModel.TILE_SIZE;
        playerModel.setPosition(x, y);
        playerModel.setStartPosition(x, y);
        playerModel.setTargetPosition(x, y);
        playerModel.setMoving(false);
        playerModel.setMovementTime(0f);
        playerModel.setStateTime(0f);
        logger.debug("Set position to ({}, {})", x, y);
    }
}

package io.github.pokemeetup.player.service.impl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.model.PlayerDirection;
import io.github.pokemeetup.player.model.PlayerModel;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import io.github.pokemeetup.input.InputService;
import io.github.pokemeetup.player.config.PlayerProperties;
import io.github.pokemeetup.world.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerServiceImpl implements PlayerService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerModel playerModel;
    private final PlayerAnimationService animationService;
    private final WorldService worldService;
    private final String username;

    // Durations for walking/running a single tile
    private final float walkStepDuration;
    private final float runStepDuration;

    private final InputService inputService;

    // To allow buffering next direction if pressed mid-step
    private PlayerDirection bufferedDirection = null;

    public PlayerServiceImpl(
            PlayerAnimationService animationService,
            InputService inputService,
            PlayerProperties playerProperties,
            WorldService worldService
    ) {
        this.playerModel = new PlayerModel(0, 0);
        this.animationService = animationService;
        this.inputService = inputService;
        this.username = playerProperties.getUsername();
        this.walkStepDuration = playerProperties.getWalkStepDuration();
        this.runStepDuration = playerProperties.getRunStepDuration();
        this.playerModel.setRunning(false);
        this.worldService = worldService;
    }

    @Override
    public void move(PlayerDirection direction) {
        if (playerModel.isMoving()) {
            // If already moving, buffer next direction
            logger.debug("Currently moving. Buffering direction: {}", direction);
            this.bufferedDirection = direction;
            return;
        }

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

        // Collision checks would be here if needed

        playerModel.setStartPosition(currentX, currentY);
        playerModel.setTargetPosition(targetX, targetY);

        float duration = playerModel.isRunning() ? runStepDuration : walkStepDuration;
        playerModel.setMovementDuration(duration);

        playerModel.setStateTime(0f);
        playerModel.setMovementTime(0f);
        playerModel.setMoving(true);

        logger.debug("Initiated movement: {}, Target=({}, {}), Duration={}", direction, targetX, targetY, duration);
    }

    @Override
    public void update(float delta) {
        playerModel.setStateTime(playerModel.getStateTime() + delta);

        if (playerModel.isMoving()) {
            float progress = playerModel.getMovementTime() / playerModel.getMovementDuration();
            progress = Math.min(progress + (delta / playerModel.getMovementDuration()), 1f);

            float smoothed = smoothstep(progress);

            float newX = lerp(playerModel.getStartPosition().x, playerModel.getTargetPosition().x, smoothed);
            float newY = lerp(playerModel.getStartPosition().y, playerModel.getTargetPosition().y, smoothed);
            playerModel.setPosition(newX, newY);

            playerModel.setMovementTime(playerModel.getMovementTime() + delta);

            if (progress >= 1f) {
                // Movement finished
                playerModel.setMoving(false);
                playerModel.setPosition(playerModel.getTargetPosition().x, playerModel.getTargetPosition().y);

                // Update player data in world
                worldService.setPlayerData(getPlayerData());

                // If we had a buffered direction, move immediately in that direction
                if (bufferedDirection != null) {
                    PlayerDirection nextDir = bufferedDirection;
                    bufferedDirection = null;
                    move(nextDir);
                } else {
                    // No buffered direction, check if the player is still holding a direction
                    PlayerDirection dir = inputService.getCurrentDirection();
                    if (dir != null) {
                        move(dir);
                    } else {
                        // Standing still
                        playerModel.setMoving(false);
                        playerModel.setRunning(false);
                    }
                }
            }
        } else {
            // Not moving, check if direction is pressed
            PlayerDirection dir = inputService.getCurrentDirection();
            if (dir != null) {
                move(dir);
            } else {
                playerModel.setMoving(false);
                playerModel.setRunning(false);
            }
        }
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float smoothstep(float x) {
        x = Math.max(0f, Math.min(x, 1f));
        return x * x * (3f - 2f * x);
    }

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

    @Override
    public PlayerData getPlayerData() {
        float tileX = playerModel.getPosition().x / PlayerModel.TILE_SIZE;
        float tileY = playerModel.getPosition().y / PlayerModel.TILE_SIZE;
        return new PlayerData(username, tileX, tileY);
    }

    @Override
    public void setRunning(boolean running) {
        playerModel.setRunning(running);
        logger.debug("Set running to {}", running);
    }

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
        this.bufferedDirection = null;
        logger.debug("Set position to ({}, {})", x, y);
    }
}

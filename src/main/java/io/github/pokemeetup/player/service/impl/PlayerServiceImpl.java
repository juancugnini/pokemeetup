package io.github.pokemeetup.player.service.impl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.model.PlayerDirection;
import io.github.pokemeetup.player.model.PlayerModel;
import io.github.pokemeetup.player.service.PlayerAnimationService;
import io.github.pokemeetup.player.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class PlayerServiceImpl implements PlayerService {

    private final PlayerModel playerModel;
    private final PlayerAnimationService animationService;
    private final String username; 

    @Autowired
    public PlayerServiceImpl(PlayerAnimationService animationService) {
        
        this(animationService, "Player");
    }

    public PlayerServiceImpl(PlayerAnimationService animationService, String username) {
        this.playerModel = new PlayerModel(0,0);
        this.animationService = animationService;
        this.username = username;
    }

    @Override
    public void move(PlayerDirection direction) {
        if (playerModel.isMoving()) {
            return;
        }

        playerModel.setDirection(direction);

        float currentX = playerModel.getPosition().x;
        float currentY = playerModel.getPosition().y;
        float tileSize = PlayerModel.TILE_SIZE;

        float targetX = currentX;
        float targetY = currentY;

        switch (direction) {
            case UP:
                targetY += tileSize;
                break;
            case DOWN:
                targetY -= tileSize;
                break;
            case LEFT:
                targetX -= tileSize;
                break;
            case RIGHT:
                targetX += tileSize;
                break;
        }

        playerModel.setStartPosition(currentX, currentY);
        playerModel.setTargetPosition(targetX, targetY);
        playerModel.setMoving(true);
        playerModel.setMovementTime(0f);
    }

    @Override
    public void update(float delta) {
        if (playerModel.isMoving()) {
            float speedMultiplier = playerModel.isRunning() ? playerModel.getRunSpeedMultiplier() : 1.0f;
            float progress = (playerModel.getMovementTime() + delta / playerModel.getMovementDuration() * speedMultiplier);

            if (progress >= 1f) {
                progress = 1f;
                playerModel.setMoving(false);
            }

            playerModel.setMovementTime(progress);

            float startX = playerModel.getStartPosition().x;
            float startY = playerModel.getStartPosition().y;
            float endX = playerModel.getTargetPosition().x;
            float endY = playerModel.getTargetPosition().y;

            float smooth = smoothstep(progress);
            float newX = MathUtils.lerp(startX, endX, smooth);
            float newY = MathUtils.lerp(startY, endY, smooth);

            playerModel.setPosition(newX, newY);
        }

        playerModel.setStateTime(playerModel.getStateTime() + delta);
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
    }

    private float smoothstep(float x) {
        x = MathUtils.clamp(x, 0f, 1f);
        return x * x * (3 - 2 * x);
    }
}

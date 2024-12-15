package io.github.pokemeetup.player.model;

import com.badlogic.gdx.math.Vector2;

public class PlayerModel {
    public static final int TILE_SIZE = 32;

    private Vector2 position;     
    private Vector2 startPosition;
    private Vector2 targetPosition;
    private PlayerDirection direction;
    private boolean moving;
    private boolean running;

    
    private float movementTime = 0f;        
    private float movementDuration = 0.2f;  
    private float runSpeedMultiplier = 1.75f;

    
    private float stateTime = 0f;

    public PlayerModel(int startTileX, int startTileY) {
        float x = startTileX * TILE_SIZE;
        float y = startTileY * TILE_SIZE;
        this.position = new Vector2(x, y);
        this.targetPosition = new Vector2(x, y);
        this.startPosition = new Vector2(x, y);
        this.direction = PlayerDirection.DOWN;
        this.moving = false;
        this.running = false;
    }

    public Vector2 getPosition() { return position; }
    public Vector2 getTargetPosition() { return targetPosition; }
    public Vector2 getStartPosition() { return startPosition; }

    public void setDirection(PlayerDirection direction) { this.direction = direction; }
    public PlayerDirection getDirection() { return direction; }

    public boolean isMoving() { return moving; }
    public void setMoving(boolean moving) { this.moving = moving; }

    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }

    public float getMovementTime() { return movementTime; }
    public void setMovementTime(float t) { this.movementTime = t; }

    public float getMovementDuration() { return movementDuration; }

    public float getRunSpeedMultiplier() { return runSpeedMultiplier; }

    public float getStateTime() { return stateTime; }
    public void setStateTime(float stateTime) { this.stateTime = stateTime; }

    public void setTargetPosition(float x, float y) {
        this.targetPosition.set(x, y);
    }

    public void setStartPosition(float x, float y) {
        this.startPosition.set(x, y);
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
}

package io.github.pokemeetup.player.service;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.model.PlayerDirection;

public interface PlayerService {
    void move(PlayerDirection direction);
    void update(float delta);
    void render(SpriteBatch batch);

    PlayerData getPlayerData();
    void setPlayerData(PlayerData data);

    void setRunning(boolean running);
    void setPosition(int tileX, int tileY);
}

package io.github.pokemeetup.multiplayer.model;

import lombok.Getter;
import lombok.Setter;
import io.github.pokemeetup.player.model.PlayerData;

@Getter @Setter
public class PlayerSyncData {
    private String username;
    private float x;
    private float y;
    private boolean running;
    private String direction;
    private boolean moving;

    public static PlayerSyncData fromPlayerData(PlayerData pd) {
        PlayerSyncData sync = new PlayerSyncData();
        sync.setUsername(pd.getUsername());
        sync.setX(pd.getX());
        sync.setY(pd.getY());
        sync.setRunning(pd.isWantsToRun());
        sync.setDirection(pd.getDirection() != null ? pd.getDirection().name() : "DOWN");
        sync.setMoving(pd.isMoving());
        return sync;
    }
}

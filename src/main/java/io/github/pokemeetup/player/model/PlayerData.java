package io.github.pokemeetup.player.model;

import io.github.pokemeetup.world.model.WorldData;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
public class PlayerData {
    @Id
    private String username;

    private float x;
    private float y;
    private boolean wantsToRun;
    private boolean moving;
    private PlayerDirection direction = PlayerDirection.DOWN;
    private String worldName;
    @Transient
    private WorldData worldData;

    public PlayerData() {}

    public PlayerData(String username, float x, float y, WorldData worldData) {
        this.username = username;
        this.x = x;
        this.y = y;
        this.wantsToRun = false;
        this.moving = false;
        this.worldData = worldData;
        this.worldName = worldData.getWorldName();
    }
}

package io.github.pokemeetup.player.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    public PlayerData() {}

    public PlayerData(String username, float x, float y) {
        this.username = username;
        this.x = x;
        this.y = y;
        this.wantsToRun = false;
        this.moving = false;
    }
}

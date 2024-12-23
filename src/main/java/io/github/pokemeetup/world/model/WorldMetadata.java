package io.github.pokemeetup.world.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class WorldMetadata {

    private String worldName;

    private long seed;
    private long createdDate;
    private long lastPlayed;
    private long playedTime;
}

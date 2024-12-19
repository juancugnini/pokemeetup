package io.github.pokemeetup.world.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class WorldMetadata {

    @Id
    private String worldName;

    private long seed;
    private long createdDate;
    private long lastPlayed;
    private long playedTime;
}

package io.github.pokemeetup.world.model;

import java.util.HashMap;
import java.util.Map;

import io.github.pokemeetup.player.model.PlayerData;
import lombok.Getter;
import lombok.Setter;

@Getter
public class WorldData {
    @Setter
    private String worldName;
    @Setter
    private long seed;
    private Map<String, PlayerData> players = new HashMap<>();
    private Map<String, ChunkData> chunks = new HashMap<>();

    @Setter
    private long createdDate;
    @Setter
    private long lastPlayed;
    @Setter
    private long playedTime; 
}

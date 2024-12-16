package io.github.pokemeetup.world.model;

import io.github.pokemeetup.player.model.PlayerData;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class WorldData {
    private String worldName;
    private long seed;
    private Map<String, PlayerData> players = new HashMap<>();
    private Map<String, int[][]> chunks = new HashMap<>();

    public WorldData() {
    }

    public WorldData(long seed) {
        this.seed = seed;
    }

    public WorldData(String worldName, long seed) {
        this.worldName = worldName;
        this.seed = seed;
    }
}

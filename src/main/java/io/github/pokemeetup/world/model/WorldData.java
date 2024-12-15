package io.github.pokemeetup.world.model;

import io.github.pokemeetup.player.model.PlayerData;

import java.util.HashMap;
import java.util.Map;


public class WorldData {
    private long seed;
    private Map<String, PlayerData> players = new HashMap<>();
    
    
    private Map<String, int[][]> chunks = new HashMap<>();

    public WorldData() {
    }

    public WorldData(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public Map<String, PlayerData> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, PlayerData> players) {
        this.players = players;
    }

    public Map<String, int[][]> getChunks() {
        return chunks;
    }

    public void setChunks(Map<String, int[][]> chunks) {
        this.chunks = chunks;
    }
}

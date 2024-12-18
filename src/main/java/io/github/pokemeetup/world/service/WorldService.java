package io.github.pokemeetup.world.service;

import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.world.model.WorldData;

import java.util.List;

public interface WorldService {
    void initIfNeeded();
    WorldData getWorldData();

    boolean createWorld(String worldName, long seed);
    void loadWorld(String worldName);
    void saveWorldData();
    void loadWorldData();

    List<String> getAvailableWorlds();
    void deleteWorld(String worldName);

    // Player management
    void setPlayerData(PlayerData playerData);
    PlayerData getPlayerData(String username);

    // Chunk management
    int[][] getChunkTiles(int chunkX, int chunkY);
    void regenerateChunk(int chunkX, int chunkY);

    // New method for generating a world thumbnail
    void generateWorldThumbnail(String worldName);
}

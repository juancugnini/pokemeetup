package io.github.pokemeetup.world.service;

import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.world.model.WorldData;

import java.util.List;

public interface WorldService {
    void initialize();
    WorldData getWorldData();
    void saveWorldData();
    void loadWorldData();

    int[][] getChunkTiles(int chunkX, int chunkY);
    void setPlayerData(PlayerData playerData);
    PlayerData getPlayerData(String username);

    List<String> getAvailableWorlds();
    void loadWorld(String worldName);

    void deleteWorld(String worldName);
}
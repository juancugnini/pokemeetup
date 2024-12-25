package io.github.pokemeetup.multiplayer.service;

import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.multiplayer.model.ChunkUpdate;
import io.github.pokemeetup.multiplayer.model.PlayerSyncData;
import io.github.pokemeetup.multiplayer.model.WorldObjectUpdate;

import java.util.List;
import java.util.Map;

public interface MultiplayerService {
    void playerConnected(String username);
    void playerDisconnected(String username);

    PlayerData getPlayerData(String username);
    void updatePlayerData(PlayerData data);

    ChunkUpdate getChunkData(int chunkX, int chunkY);
    Map<String, PlayerSyncData> getAllPlayerStates();
    List<WorldObjectUpdate> getAllWorldObjectUpdates();

    void broadcastPlayerState(PlayerData data);
    void broadcastChunkUpdate(ChunkUpdate chunk);
    void broadcastWorldObjectUpdate(WorldObjectUpdate objUpdate);
    void tick(float delta);
}

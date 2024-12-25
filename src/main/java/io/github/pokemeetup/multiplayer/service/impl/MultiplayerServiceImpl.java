package io.github.pokemeetup.multiplayer.service.impl;

import io.github.pokemeetup.multiplayer.model.ChunkUpdate;
import io.github.pokemeetup.multiplayer.model.PlayerSyncData;
import io.github.pokemeetup.multiplayer.model.WorldObjectUpdate;
import io.github.pokemeetup.multiplayer.service.MultiplayerService;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.world.model.WorldObject;
import io.github.pokemeetup.world.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiplayerServiceImpl implements MultiplayerService {

    private final WorldService worldService;


    private final Set<String> connectedPlayers = Collections.synchronizedSet(new HashSet<>());


    private final List<WorldObjectUpdate> pendingObjectUpdates = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    public MultiplayerServiceImpl(WorldService worldService) {
        this.worldService = worldService;
    }

    @Override
    public void playerConnected(String username) {
        worldService.initIfNeeded();
        PlayerData pd = worldService.getPlayerData(username);
        if (pd == null) {
            pd = new PlayerData(username, 0, 0);
            worldService.setPlayerData(pd);
        }
        connectedPlayers.add(username);
    }

    @Override
    public void playerDisconnected(String username) {
        connectedPlayers.remove(username);
    }

    @Override
    public PlayerData getPlayerData(String username) {
        return worldService.getPlayerData(username);
    }

    @Override
    public void updatePlayerData(PlayerData data) {


        worldService.setPlayerData(data);
    }

    @Override
    public ChunkUpdate getChunkData(int chunkX, int chunkY) {
        int[][] tiles = worldService.getChunkTiles(chunkX, chunkY);
        if (tiles == null) return null;

        var wd = worldService.getWorldData();
        if (wd == null) return null;

        String key = chunkX + "," + chunkY;
        var chunkData = wd.getChunks().get(key);
        if (chunkData == null) return null;

        List<WorldObject> objs = chunkData.getObjects();

        ChunkUpdate update = new ChunkUpdate();
        update.setChunkX(chunkX);
        update.setChunkY(chunkY);
        update.setTiles(tiles);
        update.setObjects(objs);
        return update;
    }

    @Override
    public Map<String, PlayerSyncData> getAllPlayerStates() {
        Map<String, PlayerSyncData> states = new HashMap<>();
        for (String user : connectedPlayers) {
            PlayerData pd = worldService.getPlayerData(user);
            if (pd != null) {
                states.put(user, PlayerSyncData.fromPlayerData(pd));
            }
        }
        return states;
    }

    @Override
    public List<WorldObjectUpdate> getAllWorldObjectUpdates() {
        List<WorldObjectUpdate> snapshot;
        synchronized (pendingObjectUpdates) {
            snapshot = new ArrayList<>(pendingObjectUpdates);
            pendingObjectUpdates.clear();
        }
        return snapshot;
    }

    @Override
    public void broadcastPlayerState(PlayerData data) {


    }

    @Override
    public void broadcastChunkUpdate(ChunkUpdate chunk) {

    }

    @Override
    public void broadcastWorldObjectUpdate(WorldObjectUpdate objUpdate) {
        synchronized (pendingObjectUpdates) {
            pendingObjectUpdates.add(objUpdate);
        }
    }

    @Override
    public void tick(float delta) {

    }
}

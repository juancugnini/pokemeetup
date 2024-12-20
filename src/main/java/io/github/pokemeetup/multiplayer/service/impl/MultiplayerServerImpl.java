package io.github.pokemeetup.multiplayer.service.impl;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import io.github.pokemeetup.NetworkProtocol;
import io.github.pokemeetup.event.EventBus;
import io.github.pokemeetup.multiplayer.service.AuthService;
import io.github.pokemeetup.multiplayer.service.MultiplayerServer;
import io.github.pokemeetup.multiplayer.service.MultiplayerService;
import io.github.pokemeetup.player.event.PlayerJoinEvent;
import io.github.pokemeetup.player.event.PlayerLeaveEvent;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.multiplayer.model.ChunkUpdate;
import io.github.pokemeetup.multiplayer.model.PlayerSyncData;
import io.github.pokemeetup.world.service.WorldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Primary
@Service
public class MultiplayerServerImpl implements MultiplayerServer {

    private final MultiplayerService multiplayerService;
    private final EventBus eventBus;
    private final AuthService authService;
    private final Map<Integer, String> connectionUserMap = new ConcurrentHashMap<>();

    private Server server;
    private volatile boolean running = false;
    @Autowired
    private WorldService worldService;

    public MultiplayerServerImpl(MultiplayerService multiplayerService,
                                 EventBus eventBus,
                                 AuthService authService) {
        this.multiplayerService = multiplayerService;
        this.eventBus = eventBus;
        this.authService = authService;
    }

    @Override
    public void startServer(int tcpPort, int udpPort) {
        if (running) {
            log.warn("Server already running.");
            return;
        }

        server = new Server();
        NetworkProtocol.registerClasses(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                log.info("New connection: {}", connection.getRemoteAddressTCP());
            }

            @Override
            public void disconnected(Connection connection) {
                handleDisconnection(connection);
            }

            @Override
            public void received(Connection connection, Object object) {
                handleMessage(connection, object);
            }
        });

        try {
            server.start();
            server.bind(tcpPort, udpPort);
            running = true;
            log.info("Multiplayer server started on TCP:{} UDP:{}", tcpPort, udpPort);
        } catch (IOException e) {
            log.error("Failed to start server: {}", e.getMessage(), e);
        }
    }

    private void handleDisconnection(Connection connection) {
        String username = connectionUserMap.remove(connection.getID());
        if (username != null) {
            multiplayerService.playerDisconnected(username);
            eventBus.fireEvent(new PlayerLeaveEvent(username));
            log.info("Player {} disconnected", username);
            broadcastPlayerStates();
        } else {
            log.info("Connection {} disconnected without a known user.", connection.getID());
        }
    }

    private void handleMessage(Connection connection, Object object) {
        if (object.getClass().getName().startsWith("com.esotericsoftware.kryonet.FrameworkMessage")) {
            return;
        }
        if (object instanceof NetworkProtocol.LoginRequest req) {
            handleLogin(connection, req);
        } else if (object instanceof NetworkProtocol.CreateUserRequest createReq) {
            handleCreateUser(connection, createReq);
        } else if (object instanceof NetworkProtocol.PlayerMoveRequest moveReq) {
            handlePlayerMove(connection, moveReq);
        } else if (object instanceof NetworkProtocol.ChunkRequest chunkReq) {
            handleChunkRequest(connection, chunkReq);
        } else if (object instanceof io.github.pokemeetup.chat.model.ChatMessage chatMsg) {
            handleChatMessage(connection, chatMsg);
        } else {
            log.warn("Unknown message type received: {}", object.getClass());
        }
    }

    private void handleChatMessage(Connection connection, io.github.pokemeetup.chat.model.ChatMessage msg) {
        String sender = connectionUserMap.get(connection.getID());
        if (sender == null) {
            log.warn("ChatMessage received from unregistered connection: {}", connection.getID());
            return;
        }
        msg.setSender(sender);

        log.info("Received ChatMessage from {}: {}", sender, msg.getContent());
        server.sendToAllExceptTCP(connection.getID(), msg);

    }

    private void handleLogin(Connection connection, NetworkProtocol.LoginRequest req) {
        boolean authSuccess = authService.authenticate(req.getUsername(), req.getPassword());
        NetworkProtocol.LoginResponse resp = new NetworkProtocol.LoginResponse();

        if (!authSuccess) {
            resp.setSuccess(false);
            resp.setMessage("Invalid username or password.");
            connection.sendTCP(resp);
            log.info("Authentication failed for user: {}", req.getUsername());
            return;
        }

        connectionUserMap.put(connection.getID(), req.getUsername());
        multiplayerService.playerConnected(req.getUsername());

        eventBus.fireEvent(new PlayerJoinEvent(req.getUsername()));
        PlayerData pd = multiplayerService.getPlayerData(req.getUsername());

        resp.setSuccess(true);
        resp.setUsername(req.getUsername());
        resp.setX((int) pd.getX());
        resp.setY((int) pd.getY());

        connection.sendTCP(resp);
        log.info("User '{}' logged in successfully from {}", req.getUsername(), connection.getRemoteAddressTCP());

        broadcastPlayerStates();
        sendInitialChunks(connection, pd);
    }

    private void handleCreateUser(Connection connection, NetworkProtocol.CreateUserRequest req) {
        NetworkProtocol.CreateUserResponse resp = new NetworkProtocol.CreateUserResponse();
        boolean success = authService.createUser(req.getUsername(), req.getPassword());
        if (success) {
            resp.setSuccess(true);
            resp.setMessage("User created successfully. You can now log in.");
        } else {
            resp.setSuccess(false);
            resp.setMessage("Username already exists or invalid input.");
        }
        connection.sendTCP(resp);
        log.info("User creation attempt for '{}': {}", req.getUsername(), success ? "SUCCESS" : "FAILURE");
    }

    private void handlePlayerMove(Connection connection, NetworkProtocol.PlayerMoveRequest moveReq) {
        String username = connectionUserMap.get(connection.getID());
        if (username == null) {
            log.warn("Received PlayerMoveRequest from unregistered connection: {}", connection.getID());
            return;
        }

        // Directly use the worldService to get and update player data
        PlayerData pd = worldService.getPlayerData(username);
        if (pd == null) {
            log.warn("PlayerData not found for username: {}", username);
            return;
        }

        // Update player position and state
        pd.setX(moveReq.getX());
        pd.setY(moveReq.getY());
        pd.setWantsToRun(moveReq.isRunning());
        pd.setMoving(moveReq.isMoving());

        try {
            pd.setDirection(io.github.pokemeetup.player.model.PlayerDirection.valueOf(moveReq.getDirection().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid direction '{}' for player '{}'", moveReq.getDirection(), username, e);
        }

        // Use the worldService directly to persist the updated player data
        worldService.setPlayerData(pd);

        // Broadcast updated player states to all clients
        broadcastPlayerStates();
    }

    private void broadcastPlayerStates() {
        Map<String, PlayerSyncData> states = multiplayerService.getAllPlayerStates();
        NetworkProtocol.PlayerStatesUpdate update = new NetworkProtocol.PlayerStatesUpdate();
        update.setPlayers(states);
        broadcast(update);
    }

    private void handleChunkRequest(Connection connection, NetworkProtocol.ChunkRequest req) {
        ChunkUpdate chunk = multiplayerService.getChunkData(req.getChunkX(), req.getChunkY());
        if (chunk == null) {
            log.warn("No chunk data found for ({}, {})", req.getChunkX(), req.getChunkY());
            return;
        }

        NetworkProtocol.ChunkData chunkData = new NetworkProtocol.ChunkData();
        chunkData.setChunkX(req.getChunkX());
        chunkData.setChunkY(req.getChunkY());
        chunkData.setTiles(chunk.getTiles());
        chunkData.setObjects(chunk.getObjects());
        connection.sendTCP(chunkData);
    }

    private void sendInitialChunks(Connection connection, PlayerData pd) {
        int px = (int) pd.getX();
        int py = (int) pd.getY();
        int radius = 2;
        int startX = px / 16 - radius;
        int endX = px / 16 + radius;
        int startY = py / 16 - radius;
        int endY = py / 16 + radius;

        for (int cx = startX; cx <= endX; cx++) {
            for (int cy = startY; cy <= endY; cy++) {
                ChunkUpdate chunk = multiplayerService.getChunkData(cx, cy);
                if (chunk == null) continue;

                NetworkProtocol.ChunkData cd = new NetworkProtocol.ChunkData();
                cd.setChunkX(cx);
                cd.setChunkY(cy);
                cd.setTiles(chunk.getTiles());
                cd.setObjects(chunk.getObjects());
                connection.sendTCP(cd);
            }
        }
    }


    @Override
    public void broadcast(Object message) {
        if (server != null && running) {
            server.sendToAllTCP(message);
        } else {
            log.warn("Cannot broadcast message, server not running.");
        }
    }

    @Override
    public void stopServer() {
        if (!running) {
            log.warn("Attempt to stop server that is not running.");
            return;
        }

        for (String username : connectionUserMap.values()) {
            eventBus.fireEvent(new PlayerLeaveEvent(username));
        }
        server.stop();
        running = false;
        log.info("Multiplayer server stopped.");
    }

    @Override
    public void processMessages(float delta) {
        multiplayerService.tick(delta);
        var objectUpdates = multiplayerService.getAllWorldObjectUpdates();
        if (!objectUpdates.isEmpty()) {
            NetworkProtocol.WorldObjectsUpdate wUpdate = new NetworkProtocol.WorldObjectsUpdate();
            wUpdate.setObjects(objectUpdates);
            broadcast(wUpdate);
        }
    }
}

package io.github.pokemeetup.multiplayer.service;

public interface MultiplayerServer {
    void startServer(int tcpPort, int udpPort);
    void stopServer();
    void broadcast(Object message);
    void processMessages(float delta);
}

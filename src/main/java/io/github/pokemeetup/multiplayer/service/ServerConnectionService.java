package io.github.pokemeetup.multiplayer.service;

import io.github.pokemeetup.multiplayer.model.ServerConnectionConfig;
import java.util.List;

public interface ServerConnectionService {
    void saveConfig(ServerConnectionConfig config);
    ServerConnectionConfig loadConfig();

    List<ServerConnectionConfig> listServers();
    void addServer(ServerConnectionConfig config);
    void deleteServer(ServerConnectionConfig config);
}

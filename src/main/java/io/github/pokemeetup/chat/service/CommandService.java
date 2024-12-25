package io.github.pokemeetup.chat.service;

import io.github.pokemeetup.chat.commands.Command;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.player.service.PlayerService;

public interface CommandService {
    void registerCommand(Command command);
    boolean executeCommand(String name, String args, PlayerService playerService, ChatService chatService, MultiplayerClient multiplayerClient);
}

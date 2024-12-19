package io.github.pokemeetup.chat.commands;

import io.github.pokemeetup.chat.service.ChatService;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.player.service.PlayerService;

public interface Command {
    String getName();
    String[] getAliases();
    String getDescription();
    String getUsage();
    boolean isMultiplayerOnly();

    
    void execute(String args, PlayerService playerService, ChatService chatService, MultiplayerClient multiplayerClient);
}

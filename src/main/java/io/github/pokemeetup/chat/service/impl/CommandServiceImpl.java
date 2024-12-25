package io.github.pokemeetup.chat.service.impl;

import io.github.pokemeetup.chat.commands.Command;
import io.github.pokemeetup.chat.service.ChatService;
import io.github.pokemeetup.chat.service.CommandService;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.player.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CommandServiceImpl implements CommandService {
    private final Map<String, Command> commands = new HashMap<>();

    @Override
    public void registerCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        log.info("Registered command: {}", command.getName());
    }

    @Override
    public boolean executeCommand(String name, String args, PlayerService playerService, ChatService chatService, MultiplayerClient multiplayerClient) {
        Command cmd = commands.get(name.toLowerCase());
        if (cmd == null) {
            return false;
        }
        try {
            if (cmd.isMultiplayerOnly() && !multiplayerClient.isConnected()) {
                chatService.addSystemMessage("This command can only be used in multiplayer.");
                return true;
            }
            cmd.execute(args, playerService, chatService, multiplayerClient);
            return true;
        } catch (Exception e) {
            log.error("Command execution failed: {}", e.getMessage(), e);
            chatService.addSystemMessage("Error executing command: " + e.getMessage());
            return true;
        }
    }
}

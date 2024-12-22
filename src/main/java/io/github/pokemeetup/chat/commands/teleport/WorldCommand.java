package io.github.pokemeetup.chat.commands.teleport;

import io.github.pokemeetup.chat.commands.Command;
import io.github.pokemeetup.chat.service.ChatService;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.player.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorldCommand implements Command {
    @Override
    public String getName() {
        return "getWorld";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "gets current world name";
    }

    @Override
    public String getUsage() {
        return "/getWorld";
    }

    @Override
    public boolean isMultiplayerOnly() {
        return false;
    }

    @Override
    public void execute(String args, PlayerService playerService, ChatService chatService, MultiplayerClient multiplayerClient) {
        String[] argsArray = args.split(" ");
        PlayerData player = playerService.getPlayerData();

        chatService.addSystemMessage(player.getWorldData().getWorldName());
    }
}

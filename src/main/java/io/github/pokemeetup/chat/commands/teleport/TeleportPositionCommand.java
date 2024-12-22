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
public class TeleportPositionCommand implements Command {

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "teleports user to location";
    }

    @Override
    public String getUsage() {
        return "tp <x> <y>";
    }

    @Override
    public boolean isMultiplayerOnly() {
        return false;
    }

    @Override
    public void execute(String args, PlayerService playerService, ChatService chatService, MultiplayerClient multiplayerClient) {
        String[] argsArray = args.split(" ");
        PlayerData player = playerService.getPlayerData();
        try {
            if (player == null) {
                chatService.addSystemMessage("Error: Player not found");
                return;
            }

            if (argsArray.length != 2) {
                chatService.addSystemMessage("Invalid arguments, use: " + getUsage());
                return;
            }

            Integer tileX = Integer.parseInt(argsArray[0]);
            Integer tileY = Integer.parseInt(argsArray[1]);

            player.setX(tileX * 32);
            player.setY(tileY * 32);
            playerService.setPosition(tileX, tileY);

            if (multiplayerClient.isConnected()) {
                multiplayerClient.sendPlayerMove(player.getX(), player.getY(), player.isWantsToRun(), player.isMoving(), player.getDirection().name().toLowerCase());
            }
            chatService.addSystemMessage("Teleported to spawn point!");
        } catch (Exception e) {
            log.error("Error executing tp command: " + e.getMessage());
        }
    }
}

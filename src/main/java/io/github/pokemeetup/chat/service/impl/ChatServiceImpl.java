package io.github.pokemeetup.chat.service.impl;

import io.github.pokemeetup.chat.event.ChatMessageReceivedEvent;
import io.github.pokemeetup.chat.model.ChatMessage;
import io.github.pokemeetup.chat.service.ChatService;
import io.github.pokemeetup.chat.service.CommandService;
import io.github.pokemeetup.multiplayer.service.MultiplayerClient;
import io.github.pokemeetup.player.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final PlayerService playerService;
    private final MultiplayerClient multiplayerClient;
    private final CommandService commandService;

    private boolean isActive;
    private final Queue<ChatMessage> messages = new LinkedList<>();
    private final List<String> messageHistory = new ArrayList<>();
    private int messageHistoryIndex = -1;


    private String currentInputBeforeHistory = "";

    @Autowired
    public ChatServiceImpl(PlayerService playerService, MultiplayerClient multiplayerClient, CommandService commandService) {
        this.playerService = playerService;
        this.multiplayerClient = multiplayerClient;
        this.commandService = commandService;
    }

    public void onChatMessageReceived(ChatMessageReceivedEvent event) {
        ChatMessage chatMessage = event.getChatMessage();
        handleIncomingMessage(chatMessage);
    }
    @Override
    public void sendMessage(String content) {
        if (content.isEmpty()) return;


        if (messageHistory.isEmpty() || !content.equals(messageHistory.get(messageHistory.size()-1))) {
            messageHistory.add(content);
            messageHistoryIndex = messageHistory.size();
        }

        boolean isMultiplayer = multiplayerClient.isConnected();

        if (content.startsWith("/")) {
            String[] parts = content.substring(1).split(" ", 2);
            String commandName = parts[0].toLowerCase();
            String args = (parts.length > 1) ? parts[1] : "";
            if (!commandService.executeCommand(commandName, args, playerService, this, multiplayerClient)) {
                addSystemMessage("Unknown command: " + commandName);
            }
            return;
        }

        String username = playerService.getPlayerData().getUsername();
        if (username == null || username.isEmpty()) {
            username = "Player";
        }

        ChatMessage msg = new ChatMessage(username, content, System.currentTimeMillis(), ChatMessage.Type.NORMAL);
        handleIncomingMessage(msg);

        if (isMultiplayer) {
            multiplayerClient.sendMessage(msg);
        }
    }

    @Override
    public void addSystemMessage(String message) {
        ChatMessage sysMsg = new ChatMessage("System", message, System.currentTimeMillis(), ChatMessage.Type.SYSTEM);
        handleIncomingMessage(sysMsg);
    }

    @Override
    public void handleIncomingMessage(ChatMessage message) {
        messages.add(message);
        log.info("Received chat message from: {} content: {}", message.getSender(), message.getContent());
    }

    @Override
    public void activateChat() {
        isActive = true;
        messageHistoryIndex = messageHistory.size();
        currentInputBeforeHistory = "";
    }

    @Override
    public void deactivateChat() {
        isActive = false;

        messageHistoryIndex = messageHistory.size();
        currentInputBeforeHistory = "";
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public Queue<ChatMessage> getMessages() {
        return messages;
    }

    @Override
    public String getPreviousHistoryMessage(String currentText) {


        if (messageHistoryIndex == messageHistory.size()) {
            currentInputBeforeHistory = currentText;
        }

        if (messageHistory.isEmpty()) {
            return currentText;
        }


        if (messageHistoryIndex > 0) {
            messageHistoryIndex--;
            return messageHistory.get(messageHistoryIndex);
        } else {

            return messageHistory.get(0);
        }
    }

    @Override
    public String getNextHistoryMessage() {
        if (messageHistory.isEmpty()) {
            return "";
        }


        if (messageHistoryIndex < messageHistory.size()) {
            messageHistoryIndex++;
        }


        if (messageHistoryIndex == messageHistory.size()) {
            return currentInputBeforeHistory;
        } else {
            return messageHistory.get(messageHistoryIndex);
        }
    }
}

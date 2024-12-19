package io.github.pokemeetup.chat.service;

import io.github.pokemeetup.chat.model.ChatMessage;

import java.util.Queue;

public interface ChatService {
    void sendMessage(String content);
    void addSystemMessage(String message);
    void handleIncomingMessage(ChatMessage message);
    void activateChat();
    void deactivateChat();
    boolean isActive();

    Queue<ChatMessage> getMessages();

    
    String getPreviousHistoryMessage(String currentText);

    
    String getNextHistoryMessage();
}

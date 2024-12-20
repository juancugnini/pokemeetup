package io.github.pokemeetup.chat.event;

import io.github.pokemeetup.chat.model.ChatMessage;

public interface ChatListener {
    void onNewMessage(ChatMessage msg);
}

package io.github.pokemeetup.chat.event;

import io.github.pokemeetup.chat.model.ChatMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatMessageReceivedEvent extends ApplicationEvent {
    private final ChatMessage chatMessage;

    public ChatMessageReceivedEvent(Object source, ChatMessage chatMessage) {
        super(source);
        this.chatMessage = chatMessage;
    }

}

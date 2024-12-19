package io.github.pokemeetup.player.event;

import io.github.pokemeetup.event.Event;
import lombok.Getter;

@Getter
public class PlayerJoinEvent implements Event {
    private final String username;

    public PlayerJoinEvent(String username) {
        this.username = username;
    }

}

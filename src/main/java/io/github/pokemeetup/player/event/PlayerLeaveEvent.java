package io.github.pokemeetup.player.event;

import io.github.pokemeetup.event.Event;
import lombok.Getter;

@Getter
public class PlayerLeaveEvent implements Event {
    private final String username;

    public PlayerLeaveEvent(String username) {
        this.username = username;
    }

}

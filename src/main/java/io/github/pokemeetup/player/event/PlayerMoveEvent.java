package io.github.pokemeetup.player.event;

import io.github.pokemeetup.event.Event;
import io.github.pokemeetup.player.model.PlayerData;
import lombok.Getter;

@Getter
public class PlayerMoveEvent implements Event {
    private final PlayerData playerData;

    public PlayerMoveEvent(PlayerData playerData) {
        this.playerData = playerData;
    }

}

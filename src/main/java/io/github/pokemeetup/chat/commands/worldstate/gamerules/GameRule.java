package io.github.pokemeetup.chat.commands.worldstate.gamerules;

import io.github.pokemeetup.chat.commands.Command;

public interface GameRule extends Command {
    @Override
    default String getName() {
        return "gamerule";
    }
}
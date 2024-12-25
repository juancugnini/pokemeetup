package io.github.pokemeetup.chat.config;

import io.github.pokemeetup.chat.commands.SpawnCommand;
import io.github.pokemeetup.chat.service.CommandService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Autowired
    private CommandService commandService;

    @Autowired
    private SpawnCommand spawnCommand;

    @PostConstruct
    public void registerCommands() {
        commandService.registerCommand(spawnCommand);
    }
}

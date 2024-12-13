package io.github.pokemeetup.world.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class World {
    private String worldName;
    public void doSomething() {
        log.info("doSomething");
    }
}

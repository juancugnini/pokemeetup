package io.github.pokemeetup.world.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Setter
@Getter
public class BiomeConfig {
    private HashMap<String, HashMap<String, Integer>> biomes;
}


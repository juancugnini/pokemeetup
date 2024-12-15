package io.github.pokemeetup.audio.model;

public enum WeatherSoundEffect {
    LIGHT_RAIN("sounds/weather/rain.ogg"),
    THUNDER("sounds/weather/thunder.ogg"),
    WIND("sounds/weather/wind.ogg"),
    SAND_WIND("sounds/weather/sandwind.ogg");

    private final String path;

    WeatherSoundEffect(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

package io.github.pokemeetup.player.model;

public class PlayerData {
    private String username;
    private float x;
    private float y;

    public PlayerData() {
    }

    public PlayerData(String username, float x, float y) {
        this.username = username;
        this.x = x;
        this.y = y;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
}

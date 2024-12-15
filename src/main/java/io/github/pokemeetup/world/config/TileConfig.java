package io.github.pokemeetup.world.config;

import java.util.List;

public class TileConfig {
    private List<TileDefinition> tiles;

    public List<TileDefinition> getTiles() {
        return tiles;
    }

    public void setTiles(List<TileDefinition> tiles) {
        this.tiles = tiles;
    }

    public static class TileDefinition {
        private int id;
        private String name;
        private String texture;
        private boolean passable;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) { this.name = name; }

        public String getTexture() {
            return texture;
        }

        public void setTexture(String texture) { this.texture = texture; }

        public boolean isPassable() {
            return passable;
        }

        public void setPassable(boolean passable) {
            this.passable = passable;
        }
    }
}

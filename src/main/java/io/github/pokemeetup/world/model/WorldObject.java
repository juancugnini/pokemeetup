package io.github.pokemeetup.world.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorldObject {
    private String id;
    private int tileX;
    private int tileY;
    private ObjectType type;
    private transient TextureRegion texture; // not persisted
    private float spawnTime;
    private boolean collidable;

    public WorldObject(int tileX, int tileY, ObjectType type, TextureRegion texture) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.type = type;
        this.texture = texture;
        this.id = java.util.UUID.randomUUID().toString();
        this.collidable = type.isCollidable();
        this.spawnTime = type.isPermanent() ? 0f : (System.currentTimeMillis() / 1000f);
    }

    public WorldObject() {}

    public float getPixelX() {
        return tileX * 32; // assuming 32px tiles
    }

    public float getPixelY() {
        return tileY * 32;
    }

    public boolean isExpired(float despawnTime) {
        if (type.isPermanent()) return false;
        float currentTime = System.currentTimeMillis() / 1000f;
        return currentTime - spawnTime > despawnTime;
    }
}

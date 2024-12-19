package io.github.pokemeetup.world.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.*;
import java.util.List;

@Entity
@Data
public class ChunkData {

    @EmbeddedId
    private ChunkKey key;

    @Lob
    @Column(nullable = false)
    private byte[] tilesBlob;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chunk_objects", joinColumns = {
            @JoinColumn(name = "chunkX", referencedColumnName = "chunkX"),
            @JoinColumn(name = "chunkY", referencedColumnName = "chunkY")
    })
    private List<WorldObject> objects;


    public void setTiles(int[][] tiles) {
        this.tilesBlob = serializeTiles(tiles);
    }

    public int[][] getTiles() {
        return deserializeTiles(this.tilesBlob);
    }

    private byte[] serializeTiles(int[][] tiles) {
        if (tiles == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(tiles);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize tiles", e);
        }
    }

    private int[][] deserializeTiles(byte[] data) {
        if (data == null) return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (int[][]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize tiles", e);
        }
    }

    @Embeddable
    @Data
    public static class ChunkKey implements Serializable {
        private int chunkX;
        private int chunkY;

        public ChunkKey() {}

        public ChunkKey(int chunkX, int chunkY) {
            this.chunkX = chunkX;
            this.chunkY = chunkY;
        }
    }
}

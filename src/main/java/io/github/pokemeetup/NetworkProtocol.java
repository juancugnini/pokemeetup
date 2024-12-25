package io.github.pokemeetup;

import com.esotericsoftware.kryo.Kryo;
import io.github.pokemeetup.chat.model.ChatMessage;
import io.github.pokemeetup.player.model.PlayerData;
import io.github.pokemeetup.utils.UUIDSerializer;
import io.github.pokemeetup.world.model.WorldObject;
import io.github.pokemeetup.multiplayer.model.PlayerSyncData;
import io.github.pokemeetup.multiplayer.model.WorldObjectUpdate;
import lombok.Data;

import java.util.*;

public final class NetworkProtocol {

    private NetworkProtocol() {
    }

    public static void registerClasses(Kryo kryo) {
        kryo.register(UUID.class, new UUIDSerializer());
        kryo.register(java.util.UUID.class);
        kryo.register(ChatMessage.class);

        kryo.register(PlayerData.class);
        kryo.register(WorldObject.class);
        kryo.register(PlayerSyncData.class);
        kryo.register(WorldObjectUpdate.class);

        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
        kryo.register(HashSet.class);
        kryo.register(List.class);
        kryo.register(Map.class);
        kryo.register(Set.class);

        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        kryo.register(PlayerMoveRequest.class);
        kryo.register(PlayerStatesUpdate.class);
        kryo.register(ChunkRequest.class);
        kryo.register(ChunkData.class);
        kryo.register(WorldObjectsUpdate.class);

        kryo.register(CreateUserRequest.class);
        kryo.register(CreateUserResponse.class);

        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private long timestamp;
    }
    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
    }

    @Data
    public static class CreateUserResponse {
        private boolean success;
        private String message;
    }

    @Data
    public static class LoginResponse {
        private boolean success;
        private String message;
        private String username;
        private int x;
        private int y;
        private long timestamp;
    }

    @Data
    public static class PlayerMoveRequest {
        private float x;
        private float y;
        private boolean running;
        private boolean moving;
        private String direction;
    }

    @Data
    public static class PlayerStatesUpdate {
        private Map<String, PlayerSyncData> players;
    }

    @Data
    public static class ChunkRequest {
        private int chunkX;
        private int chunkY;
        private long timestamp;
    }

    @Data
    public static class ChunkData {
        private int chunkX;
        private int chunkY;
        private int[][] tiles;
        private List<WorldObject> objects;
    }

    @Data
    public static class WorldObjectsUpdate {
        private List<WorldObjectUpdate> objects;
    }
}

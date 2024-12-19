package io.github.pokemeetup.world.repository;

import io.github.pokemeetup.world.model.ChunkData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<ChunkData, ChunkData.ChunkKey> {
}

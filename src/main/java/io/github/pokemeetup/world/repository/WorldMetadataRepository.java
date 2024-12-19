package io.github.pokemeetup.world.repository;

import io.github.pokemeetup.world.model.WorldMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldMetadataRepository extends JpaRepository<WorldMetadata, String> {
}

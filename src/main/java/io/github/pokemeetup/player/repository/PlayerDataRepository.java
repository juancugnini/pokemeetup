package io.github.pokemeetup.player.repository;

import io.github.pokemeetup.player.model.PlayerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerDataRepository extends JpaRepository<PlayerData, String> {
    PlayerData findByUsername(String username);
}

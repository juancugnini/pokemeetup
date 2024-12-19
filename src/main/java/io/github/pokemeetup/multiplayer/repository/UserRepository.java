package io.github.pokemeetup.multiplayer.repository;

import io.github.pokemeetup.multiplayer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}

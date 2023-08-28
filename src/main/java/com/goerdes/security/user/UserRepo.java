package com.goerdes.security.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<UserEntity, Integer> {

  Optional<UserEntity> findByEmail(String email);

}

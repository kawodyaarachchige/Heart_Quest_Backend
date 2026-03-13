package uob.cis045.heartquest.repository;

import uob.cis045.heartquest.domain.User;

import java.util.Optional;

/**
 * Abstraction over user persistence.
 */
public interface UserRepository {

  Optional<User> findByUsername(String username);

  /** Case-insensitive lookup for login and reset-password. */
  Optional<User> findByUsernameIgnoreCase(String username);

  User insertUser(String username, String passwordHash);

  /** Updates the password for an existing user by username. */
  void updatePassword(String username, String passwordHash);
}


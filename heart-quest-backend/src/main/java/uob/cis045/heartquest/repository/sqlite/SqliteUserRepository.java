package uob.cis045.heartquest.repository.sqlite;

import uob.cis045.heartquest.domain.User;
import uob.cis045.heartquest.exception.RepositoryException;
import uob.cis045.heartquest.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link UserRepository}.
 */
public final class SqliteUserRepository implements UserRepository {

  private final String jdbcUrl;

  public SqliteUserRepository(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    try (var c = connect();
         var ps = c.prepareStatement("SELECT id, username, password_hash FROM users WHERE username = ?")) {
      ps.setString(1, username);
      try (var rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        return Optional.of(new User(rs.getLong(1), rs.getString(2), rs.getString(3)));
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB read failed", e);
    }
  }

  @Override
  public Optional<User> findByUsernameIgnoreCase(String username) {
    try (var c = connect();
         var ps = c.prepareStatement("SELECT id, username, password_hash FROM users WHERE LOWER(username) = LOWER(?)")) {
      ps.setString(1, username);
      try (var rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        return Optional.of(new User(rs.getLong(1), rs.getString(2), rs.getString(3)));
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB read failed", e);
    }
  }

  @Override
  public User insertUser(String username, String passwordHash) {
    try (var c = connect();
         var ps = c.prepareStatement(
           "INSERT INTO users(username, password_hash, created_at) VALUES (?, ?, ?)",
           Statement.RETURN_GENERATED_KEYS
         )) {
      ps.setString(1, username);
      ps.setString(2, passwordHash);
      ps.setString(3, Instant.now().toString());
      ps.executeUpdate();
      try (var keys = ps.getGeneratedKeys()) {
        if (!keys.next()) {
          throw new RepositoryException("Failed to create user");
        }
        return new User(keys.getLong(1), username, passwordHash);
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB write failed", e);
    }
  }

  @Override
  public void updatePassword(String username, String passwordHash) {
    try (var c = connect();
         var ps = c.prepareStatement("UPDATE users SET password_hash = ? WHERE username = ?")) {
      ps.setString(1, passwordHash);
      ps.setString(2, username);
      int updated = ps.executeUpdate();
      if (updated == 0) {
        throw new RepositoryException("User not found: " + username);
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB password update failed", e);
    }
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }
}


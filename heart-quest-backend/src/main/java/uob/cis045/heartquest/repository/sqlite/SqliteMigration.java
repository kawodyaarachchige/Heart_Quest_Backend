package uob.cis045.heartquest.repository.sqlite;

import uob.cis045.heartquest.exception.RepositoryException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles schema creation and migration for the SQLite database.
 */
public final class SqliteMigration {

  private final Path dbPath;
  private final String jdbcUrl;

  public SqliteMigration(Path dbPath, String jdbcUrl) {
    this.dbPath = dbPath;
    this.jdbcUrl = jdbcUrl;
  }

  public void migrate() {
    try {
      Files.createDirectories(dbPath.toAbsolutePath().getParent());
    } catch (Exception e) {
      throw new RepositoryException("Cannot create data directory", e);
    }

    try (var c = connect()) {
      try (var st = c.createStatement()) {
        st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            created_at TEXT NOT NULL
          )
          """);

        st.executeUpdate("""
          CREATE TABLE IF NOT EXISTS scores (
            user_id INTEGER PRIMARY KEY,
            best_score INTEGER NOT NULL,
            updated_at TEXT NOT NULL,
            total_games_played INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY (user_id) REFERENCES users(id)
          )
          """);
        addScoresColumnIfMissing(c);
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB migration failed", e);
    }
  }

  private void addScoresColumnIfMissing(Connection c) throws SQLException {
    try (var ps = c.prepareStatement("PRAGMA table_info(scores)"); var rs = ps.executeQuery()) {
      while (rs.next()) {
        String name = rs.getString(2);
        if ("total_games_played".equals(name)) {
          return;
        }
      }
    }
    try (var st = c.createStatement()) {
      st.executeUpdate("ALTER TABLE scores ADD COLUMN total_games_played INTEGER DEFAULT 0");
    }
    try (var st = c.createStatement()) {
      st.executeUpdate("UPDATE scores SET total_games_played = 0 WHERE total_games_played IS NULL");
    }
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }
}


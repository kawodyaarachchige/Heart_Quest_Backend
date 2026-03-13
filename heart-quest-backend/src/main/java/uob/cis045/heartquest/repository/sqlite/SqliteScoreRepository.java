package uob.cis045.heartquest.repository.sqlite;

import uob.cis045.heartquest.domain.LeaderboardEntry;
import uob.cis045.heartquest.exception.RepositoryException;
import uob.cis045.heartquest.repository.ScoreRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed implementation of {@link ScoreRepository}.
 */
public final class SqliteScoreRepository implements ScoreRepository {

  private final String jdbcUrl;

  public SqliteScoreRepository(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  @Override
  public void upsertBestScore(long userId, int score) {
    try (var c = connect()) {
      c.setAutoCommit(false);

      Integer current = null;
      try (var ps = c.prepareStatement("SELECT best_score FROM scores WHERE user_id = ?")) {
        ps.setLong(1, userId);
        try (var rs = ps.executeQuery()) {
          if (rs.next()) {
            current = rs.getInt(1);
          }
        }
      }

      if (current != null && score <= current) {
        c.rollback();
        return;
      }

      if (current == null) {
        try (var ps = c.prepareStatement("INSERT INTO scores(user_id, best_score, updated_at, total_games_played) VALUES (?, ?, ?, 0)")) {
          ps.setLong(1, userId);
          ps.setInt(2, score);
          ps.setString(3, Instant.now().toString());
          ps.executeUpdate();
        }
      } else {
        try (var ps = c.prepareStatement("UPDATE scores SET best_score = ?, updated_at = ? WHERE user_id = ?")) {
          ps.setInt(1, score);
          ps.setString(2, Instant.now().toString());
          ps.setLong(3, userId);
          ps.executeUpdate();
        }
      }

      c.commit();
    } catch (SQLException e) {
      throw new RepositoryException("DB score update failed", e);
    }
  }

  @Override
  public int getBestScore(long userId) {
    try (var c = connect();
         var ps = c.prepareStatement("SELECT best_score FROM scores WHERE user_id = ?")) {
      ps.setLong(1, userId);
      try (var rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB get best score failed", e);
    }
  }

  @Override
  public int getTotalGamesPlayed(long userId) {
    try (var c = connect();
         var ps = c.prepareStatement("SELECT COALESCE(total_games_played, 0) FROM scores WHERE user_id = ?")) {
      ps.setLong(1, userId);
      try (var rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB get total games played failed", e);
    }
  }

  @Override
  public void recordGameStarted(long userId) {
    try (var c = connect()) {
      try (var ps = c.prepareStatement("UPDATE scores SET total_games_played = total_games_played + 1 WHERE user_id = ?")) {
        ps.setLong(1, userId);
        if (ps.executeUpdate() == 0) {
          try (var ins = c.prepareStatement("INSERT INTO scores(user_id, best_score, updated_at, total_games_played) VALUES (?, 0, ?, 1)")) {
            ins.setLong(1, userId);
            ins.setString(2, Instant.now().toString());
            ins.executeUpdate();
          }
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("DB record game started failed", e);
    }
  }

  @Override
  public List<LeaderboardEntry> getTopScores(int limit) {
    var rows = new ArrayList<LeaderboardEntry>();
    try (var c = connect();
         var ps = c.prepareStatement("""
           SELECT u.username, s.best_score, s.updated_at
           FROM scores s
           JOIN users u ON u.id = s.user_id
           ORDER BY s.best_score DESC, s.updated_at DESC
           LIMIT ?
           """)) {
      ps.setInt(1, limit);
      try (var rs = ps.executeQuery()) {
        while (rs.next()) {
          rows.add(new LeaderboardEntry(rs.getString(1), rs.getInt(2), rs.getString(3)));
        }
      }
      return rows;
    } catch (SQLException e) {
      throw new RepositoryException("DB leaderboard read failed", e);
    }
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }
}


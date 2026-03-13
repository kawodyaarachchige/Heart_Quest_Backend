package uob.cis045.heartquest.repository;

import uob.cis045.heartquest.domain.LeaderboardEntry;

import java.util.List;

/**
 * Abstraction over score and leaderboard persistence.
 */
public interface ScoreRepository {

  void upsertBestScore(long userId, int score);

  /**
   * Returns the best score for the given user, or 0 if they have no recorded score.
   */
  int getBestScore(long userId);

  /**
   * Returns how many games the user has started (whole game count), or 0 if none.
   */
  int getTotalGamesPlayed(long userId);

  /**
   * Records that the user started a new game (increments total_games_played).
   */
  void recordGameStarted(long userId);

  List<LeaderboardEntry> getTopScores(int limit);
}


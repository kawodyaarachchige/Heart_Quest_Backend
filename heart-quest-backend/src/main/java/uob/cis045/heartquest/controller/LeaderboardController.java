package uob.cis045.heartquest.controller;

import io.javalin.Javalin;
import uob.cis045.heartquest.repository.ScoreRepository;

/**
 * Controller responsible for leaderboard-related HTTP endpoints.
 * <p>
 * For now this delegates directly to {@link SqliteDatabase}; a dedicated
 * service/repository abstraction will be introduced in a later refactor step.
 * </p>
 */
public final class LeaderboardController {

  private final ScoreRepository scoreRepository;

  public LeaderboardController(ScoreRepository scoreRepository) {
    this.scoreRepository = scoreRepository;
  }

  public void registerRoutes(Javalin app) {
    app.get("/api/leaderboard", ctx -> ctx.json(scoreRepository.getTopScores(10)));
  }
}


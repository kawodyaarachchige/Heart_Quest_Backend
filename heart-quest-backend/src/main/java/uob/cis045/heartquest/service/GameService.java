package uob.cis045.heartquest.service;

import io.javalin.http.HttpStatus;
import uob.cis045.heartquest.ApiError;
import uob.cis045.heartquest.client.PuzzleProvider;
import uob.cis045.heartquest.domain.GameSession;
import uob.cis045.heartquest.domain.Puzzle;
import uob.cis045.heartquest.repository.ScoreRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Application service responsible for game flow and scoring.
 * <p>
 * This service is intentionally decoupled from {@code HttpSession};
 * the web layer is responsible for mapping to and from {@link GameSession}.
 * </p>
 */
public final class GameService {

  private final ScoreRepository scoreRepository;
  private final PuzzleProvider puzzleProvider;
  private final ScoreCalculator scoreCalculator;

  public GameService(ScoreRepository scoreRepository, PuzzleProvider puzzleProvider, ScoreCalculator scoreCalculator) {
    this.scoreRepository = scoreRepository;
    this.puzzleProvider = puzzleProvider;
    this.scoreCalculator = scoreCalculator;
  }

  public void newGame(GameSession session, String modeRaw) {
    var mode = (modeRaw == null ? "" : modeRaw.trim()).toLowerCase();
    if (!mode.equals("timed") && !mode.equals("practice")) {
      throw new ApiError(HttpStatus.BAD_REQUEST, "Mode must be 'timed' or 'practice'");
    }
    scoreRepository.recordGameStarted(session.userId());
    session.setMode(mode);
    session.setScore(0);
    session.setStreak(0);
    session.setRound(0);
    session.clearPuzzles();
  }

  public Map<String, Object> getState(GameSession session) {
    var mode = session.mode();
    return Map.of(
      "username", session.username(),
      "mode", mode,
      "score", session.score(),
      "streak", session.streak(),
      "round", session.round(),
      "timeLimitSec", mode.equals("timed") ? 20 : null
    );
  }

  public Map<String, Object> createPuzzle(GameSession session) {
    incrementRound(session);

    Puzzle puzzle = puzzleProvider.fetchPuzzle();
    var id = UUID.randomUUID().toString();

    session.putPuzzle(id, new GameSession.StoredPuzzle(puzzle.solution(), puzzle.carrots(), Instant.now().toString()));

    return Map.of(
      "puzzleId", id,
      "imageDataUrl", puzzle.imageDataUrl(),
      "carrots", puzzle.carrots()
    );
  }

  public Map<String, Object> submit(GameSession session, String puzzleId, int answer) {
    var p = requirePuzzle(session, puzzleId);
    var isCorrect = answer == p.solution();
    var nextStreak = isCorrect ? session.streak() + 1 : 0;
    var scoreDelta = scoreCalculator.scoreDeltaForSubmit(isCorrect, nextStreak);
    return endRound(session, puzzleId, "submitted", isCorrect, answer, p.solution(), p.carrots(), scoreDelta, nextStreak);
  }

  public Map<String, Object> skip(GameSession session, String puzzleId) {
    var p = requirePuzzle(session, puzzleId);
    return endRound(session, puzzleId, "skipped", false, null, p.solution(), p.carrots(), scoreCalculator.scoreDeltaForSkip(), 0);
  }

  public Map<String, Object> timeout(GameSession session, String puzzleId) {
    var p = requirePuzzle(session, puzzleId);
    return endRound(session, puzzleId, "timeout", false, null, p.solution(), p.carrots(), scoreCalculator.scoreDeltaForTimeout(), 0);
  }

  private Map<String, Object> endRound(
    GameSession session,
    String puzzleId,
    String reason,
    boolean isCorrect,
    Integer givenAnswer,
    int solution,
    int carrots,
    int scoreDelta,
    int nextStreak
  ) {
    session.removePuzzle(puzzleId);

    session.setStreak(nextStreak);
    var newScore = Math.max(0, session.score() + scoreDelta);
    session.setScore(newScore);

    scoreRepository.upsertBestScore(session.userId(), newScore);

    return Map.of(
      "isCorrect", isCorrect,
      "reason", reason,
      "givenAnswer", givenAnswer,
      "solution", solution,
      "carrots", carrots,
      "scoreDelta", scoreDelta,
      "scoreTotal", newScore,
      "streak", nextStreak,
      "round", session.round()
    );
  }

  private void incrementRound(GameSession session) {
    session.setRound(session.round() + 1);
  }

  private GameSession.StoredPuzzle requirePuzzle(GameSession session, String puzzleId) {
    if (puzzleId == null || puzzleId.isBlank()) {
      throw new ApiError(HttpStatus.BAD_REQUEST, "Missing puzzleId");
    }
    var p = session.puzzles().get(puzzleId);
    if (p == null) {
      throw new ApiError(HttpStatus.GONE, "Puzzle expired or not found");
    }
    return p;
  }
}


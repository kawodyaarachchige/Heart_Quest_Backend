package uob.cis045.heartquest.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregate representing the in-progress game state for a single user.
 * <p>
 * This is intentionally decoupled from any HTTP/session technology; controllers
 * are responsible for mapping between {@code HttpSession} and this domain type.
 * </p>
 */
public final class GameSession {

  private final long userId;
  private final String username;

  private String mode;
  private int score;
  private int streak;
  private int round;

  private final Map<String, StoredPuzzle> puzzles = new HashMap<>();

  public GameSession(long userId, String username, String mode) {
    this.userId = userId;
    this.username = username;
    this.mode = mode;
  }

  public long userId() {
    return userId;
  }

  public String username() {
    return username;
  }

  public String mode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public int score() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public int streak() {
    return streak;
  }

  public void setStreak(int streak) {
    this.streak = streak;
  }

  public int round() {
    return round;
  }

  public void setRound(int round) {
    this.round = round;
  }

  public Map<String, StoredPuzzle> puzzles() {
    return Collections.unmodifiableMap(puzzles);
  }

  public void putPuzzle(String id, StoredPuzzle puzzle) {
    puzzles.put(id, puzzle);
  }

  public StoredPuzzle removePuzzle(String id) {
    return puzzles.remove(id);
  }

  public void clearPuzzles() {
    puzzles.clear();
  }

  /**
   * Value object representing a puzzle tracked within a session.
   */
  public record StoredPuzzle(int solution, int carrots, String createdAtIso) {
  }
}


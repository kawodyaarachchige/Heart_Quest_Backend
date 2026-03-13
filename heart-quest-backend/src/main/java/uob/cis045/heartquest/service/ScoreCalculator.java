package uob.cis045.heartquest.service;

/**
 * Encapsulates scoring rules for the game.
 */
public final class ScoreCalculator {

  /**
   * Calculates the score delta for a submitted answer.
   *
   * @param isCorrect whether the answer was correct
   * @param nextStreak the streak that will apply after this round
   */
  public int scoreDeltaForSubmit(boolean isCorrect, int nextStreak) {
    if (!isCorrect) {
      return -25;
    }
    // Original rule: 100 base + 20 per additional streak beyond the first.
    return 100 + Math.max(0, nextStreak - 1) * 20;
  }

  public int scoreDeltaForSkip() {
    return -10;
  }

  public int scoreDeltaForTimeout() {
    return -25;
  }
}


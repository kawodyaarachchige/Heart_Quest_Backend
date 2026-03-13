package uob.cis045.heartquest.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {

  private final ScoreCalculator calculator = new ScoreCalculator();

  @Test
  void scoreDeltaForSubmit_correctAnswerIncreasesWithStreak() {
    Assertions.assertEquals(100, calculator.scoreDeltaForSubmit(true, 1));
    Assertions.assertEquals(120, calculator.scoreDeltaForSubmit(true, 2));
    Assertions.assertEquals(140, calculator.scoreDeltaForSubmit(true, 3));
  }

  @Test
  void scoreDeltaForSubmit_incorrectAnswerPenalty() {
    Assertions.assertEquals(-25, calculator.scoreDeltaForSubmit(false, 0));
  }

  @Test
  void scoreDeltaForSkip_penalty() {
    Assertions.assertEquals(-10, calculator.scoreDeltaForSkip());
  }

  @Test
  void scoreDeltaForTimeout_penalty() {
    Assertions.assertEquals(-25, calculator.scoreDeltaForTimeout());
  }
}


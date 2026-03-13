package uob.cis045.heartquest.client;

import uob.cis045.heartquest.domain.Puzzle;

/**
 * Abstraction over any external provider of puzzles.
 */
public interface PuzzleProvider {

  Puzzle fetchPuzzle();
}


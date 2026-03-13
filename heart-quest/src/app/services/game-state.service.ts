import { Injectable } from '@angular/core';
import { computed, signal } from '@angular/core';
import { GameMode, HeartPuzzle, PlayerIdentity, RoundResult } from '../models/heart.models';

@Injectable({
  providedIn: 'root'
})
export class GameStateService {
  private readonly _player = signal<PlayerIdentity | null>(null);
  private readonly _mode = signal<GameMode>('timed');
  private readonly _score = signal(0);
  private readonly _highestScore = signal(0);
  private readonly _streak = signal(0);
  private readonly _round = signal(0);
  private readonly _totalGamesPlayed = signal(0);
  private readonly _currentPuzzle = signal<HeartPuzzle | null>(null);
  private readonly _roundStartedAtMs = signal<number | null>(null);
  private readonly _timeLeftSec = signal<number | null>(null);
  private readonly _lastResult = signal<RoundResult | null>(null);

  player = this._player.asReadonly();
  mode = this._mode.asReadonly();
  score = this._score.asReadonly();
  highestScore = this._highestScore.asReadonly();
  streak = this._streak.asReadonly();
  round = this._round.asReadonly();
  totalGamesPlayed = this._totalGamesPlayed.asReadonly();
  currentPuzzle = this._currentPuzzle.asReadonly();
  timeLeftSec = this._timeLeftSec.asReadonly();
  lastResult = this._lastResult.asReadonly();

  playerName = computed(() => this._player()?.username ?? 'Guest');
  isInRound = computed(() => !!this._currentPuzzle());

  setPlayer(player: PlayerIdentity): void {
    this._player.set(player);
  }

  /** Restore highest score from backend (e.g. after session restore). */
  setHighestScore(value: number): void {
    this._highestScore.set(Math.max(0, value));
  }

  /** Restore total games played from backend. */
  setTotalGamesPlayed(value: number): void {
    this._totalGamesPlayed.set(Math.max(0, value));
  }

  startNewGame(player: PlayerIdentity, mode: GameMode) {
    this._player.set(player);
    this._mode.set(mode);
    this._score.set(0);
    this._streak.set(0);
    this._round.set(0);
    this._currentPuzzle.set(null);
    this._roundStartedAtMs.set(null);
    this._timeLeftSec.set(null);
    this._lastResult.set(null);
    this._totalGamesPlayed.update((n) => n + 1);
  }

  startRound(puzzle: HeartPuzzle, timeLimitSec: number | null) {
    this._currentPuzzle.set(puzzle);
    this._roundStartedAtMs.set(Date.now());
    this._timeLeftSec.set(timeLimitSec);
  }

  setTimeLeft(seconds: number | null) {
    this._timeLeftSec.set(seconds);
  }

  tick() {
    const current = this._timeLeftSec();
    if (current == null) return;
    this._timeLeftSec.set(Math.max(0, current - 1));
  }

  applyServerResult(result: RoundResult) {
    this._lastResult.set(result);
    this._streak.set(result.streak);
    this._score.set(result.scoreTotal);
    this._highestScore.update((prev) => Math.max(prev, result.scoreTotal));
    this._round.set(result.round);
    this._currentPuzzle.set(null);
    this._roundStartedAtMs.set(null);
    this._timeLeftSec.set(null);
  }

  hasActiveGame(): boolean {
    return this._player() != null;
  }

  /** Clear session (e.g. on logout). Resets all state so the next user sees their own data. */
  clearSession(): void {
    this._player.set(null);
    this._score.set(0);
    this._highestScore.set(0);
    this._streak.set(0);
    this._round.set(0);
    this._totalGamesPlayed.set(0);
    this._currentPuzzle.set(null);
    this._roundStartedAtMs.set(null);
    this._timeLeftSec.set(null);
    this._lastResult.set(null);
  }

  private requirePuzzle(): HeartPuzzle {
    const puzzle = this._currentPuzzle();
    if (!puzzle) {
      throw new Error('No active puzzle. Start a round first.');
    }
    return puzzle;
  }
}

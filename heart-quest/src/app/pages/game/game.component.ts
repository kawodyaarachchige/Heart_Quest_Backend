import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { BackendApiService } from '../../services/backend-api.service';
import { GameStateService } from '../../services/game-state.service';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-game',
  imports: [CommonModule, FormsModule, PageHeaderComponent],
  templateUrl: './game.component.html',
  styleUrl: './game.component.scss'
})
export class GameComponent implements OnInit, OnDestroy {
  answerText = '';
  loading = false;
  errorMsg: string | null = null;

  private timerSub: Subscription | null = null;
  private timeoutHandled = false;

  readonly keypadDigits = [1, 2, 3, 4, 5, 6, 7, 8, 9, 0];

  constructor(
    private readonly router: Router,
    private readonly api: BackendApiService,
    public readonly gameState: GameStateService,
  ) {}

  ngOnInit(): void {
    if (!this.gameState.hasActiveGame()) {
      void this.router.navigateByUrl('/auth');
      return;
    }
    this.loadNextPuzzle();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  loadNextPuzzle() {
    this.stopTimer();
    this.timeoutHandled = false;
    this.loading = true;
    this.errorMsg = null;
    this.answerText = '';

    this.api.getPuzzle().subscribe({
      next: (puzzle) => {
        const timeLimit = this.gameState.mode() === 'timed' ? 20 : null;
        this.gameState.startRound(puzzle, timeLimit);
        this.loading = false;
        if (timeLimit != null) {
          this.startTimer();
        }
      },
      error: (err) => {
        this.loading = false;
        if (err?.status === 401) {
          this.gameState.clearSession();
          void this.router.navigateByUrl('/auth');
          return;
        }
        this.errorMsg = 'Failed to load a new puzzle. Please try again.';
      }
    });
  }

  submit() {
    const value = Number(this.answerText);
    if (!Number.isFinite(value)) return;
    this.stopTimer();
    const puzzleId = this.gameState.currentPuzzle()?.puzzleId;
    if (!puzzleId) return;
    this.api.submit(puzzleId, value).subscribe({
      next: (result) => {
        this.gameState.applyServerResult(result);
        void this.router.navigateByUrl('/results');
      },
      error: (err) => {
        if (err?.status === 401) {
          this.gameState.clearSession();
          void this.router.navigateByUrl('/auth');
          return;
        }
        this.errorMsg = 'Submit failed. Try again.';
      }
    });
  }

  skip() {
    this.stopTimer();
    const puzzleId = this.gameState.currentPuzzle()?.puzzleId;
    if (!puzzleId) return;
    this.api.skip(puzzleId).subscribe({
      next: (result) => {
        this.gameState.applyServerResult(result);
        void this.router.navigateByUrl('/results');
      },
      error: (err) => {
        if (err?.status === 401) {
          this.gameState.clearSession();
          void this.router.navigateByUrl('/auth');
          return;
        }
        this.errorMsg = 'Skip failed. Try again.';
      }
    });
  }

  clear() {
    this.answerText = '';
  }

  backspace() {
    this.answerText = this.answerText.slice(0, -1);
  }

  pressDigit(digit: number) {
    this.answerText = `${this.answerText}${digit}`;
  }

  goHome() {
    this.stopTimer();
    void this.router.navigateByUrl('/home');
  }

  private startTimer() {
    this.stopTimer();
    this.timerSub = interval(1000).subscribe(() => {
      this.gameState.tick();
      if (!this.timeoutHandled && this.gameState.timeLeftSec() === 0) {
        this.timeoutHandled = true;
        this.onTimeout();
      }
    });
  }

  private stopTimer() {
    this.timerSub?.unsubscribe();
    this.timerSub = null;
  }

  private onTimeout() {
    this.stopTimer();
    const puzzleId = this.gameState.currentPuzzle()?.puzzleId;
    if (!puzzleId) return;
    this.api.timeout(puzzleId).subscribe({
      next: (result) => {
        this.gameState.applyServerResult(result);
        void this.router.navigateByUrl('/results');
      },
      error: (err) => {
        if (err?.status === 401) {
          this.gameState.clearSession();
          void this.router.navigateByUrl('/auth');
          return;
        }
        void this.router.navigateByUrl('/results');
      }
    });
  }
}

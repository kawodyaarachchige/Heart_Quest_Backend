import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { GameStateService } from '../../services/game-state.service';
import { interval, Subject, takeUntil } from 'rxjs';

const LOADER_MESSAGES = [
  'Loading puzzles…',
  'Preparing challenge…',
  'Get Ready!',
];

const LOAD_DURATION_MS = 2800;

@Component({
  selector: 'app-splash',
  standalone: true,
  templateUrl: './splash.component.html',
  styleUrl: './splash.component.scss',
})
export class SplashComponent implements OnInit, OnDestroy {
  readonly hearts = [0, 1, 2, 3, 4];
  readonly loadingDone = signal(false);
  readonly loaderMessage = signal(LOADER_MESSAGES[0]);
  readonly showStartButton = computed(() => this.loadingDone());

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly gameState: GameStateService,
  ) {}

  ngOnInit(): void {
    this.runLoader();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  tapToStart(): void {
    if (this.gameState.hasActiveGame()) {
      void this.router.navigateByUrl('/home');
    } else {
      void this.router.navigateByUrl('/auth');
    }
  }

  private runLoader(): void {
    const start = Date.now();
    const messageInterval = Math.floor(LOAD_DURATION_MS / LOADER_MESSAGES.length);
    let messageIndex = 0;

    const tick = () => {
      const elapsed = Date.now() - start;
      const p = Math.min(100, (elapsed / LOAD_DURATION_MS) * 100);

      const nextMsgIndex = Math.min(
        Math.floor(elapsed / messageInterval),
        LOADER_MESSAGES.length - 1,
      );
      if (nextMsgIndex !== messageIndex) {
        messageIndex = nextMsgIndex;
        this.loaderMessage.set(LOADER_MESSAGES[messageIndex]);
      }

      if (p >= 100) {
        this.loadingDone.set(true);
        return;
      }
    };

    interval(50)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => tick());
    tick();
  }
}

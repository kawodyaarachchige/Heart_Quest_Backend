import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BackendApiService } from '../../services/backend-api.service';
import { GameStateService } from '../../services/game-state.service';
import { GameMode } from '../../models/heart.models';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [PageHeaderComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  /** False until we have confirmed session (either have player or got 401). */
  sessionCheckDone = false;

  constructor(
    private readonly router: Router,
    private readonly api: BackendApiService,
    public readonly gameState: GameStateService,
  ) {}

  ngOnInit(): void {
    this.api.me().subscribe({
      next: (res) => {
        this.gameState.setPlayer({ id: res.id, username: res.username });
        this.gameState.setHighestScore(res.bestScore ?? 0);
        this.gameState.setTotalGamesPlayed(res.totalGamesPlayed ?? 0);
        this.sessionCheckDone = true;
      },
      error: () => {
        this.sessionCheckDone = true;
        this.gameState.clearSession();
        void this.router.navigateByUrl('/auth');
      },
    });
  }

  startMode(mode: GameMode): void {
    const player = this.gameState.player();
    if (!player) {
      void this.router.navigateByUrl('/auth');
      return;
    }
    this.api.newGame(mode).subscribe({
      next: () => {
        this.gameState.startNewGame(player, mode);
        void this.router.navigateByUrl('/game');
      },
      error: () => {
        void this.router.navigateByUrl('/auth');
      },
    });
  }

}

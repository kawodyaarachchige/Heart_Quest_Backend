import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LeaderboardEntry } from '../../models/heart.models';
import { BackendApiService } from '../../services/backend-api.service';
import { GameStateService } from '../../services/game-state.service';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-results',
  imports: [CommonModule, PageHeaderComponent],
  templateUrl: './results.component.html',
  styleUrl: './results.component.scss'
})
export class ResultsComponent implements OnInit {
  top: LeaderboardEntry[] = [];

  constructor(
    private readonly router: Router,
    public readonly gameState: GameStateService,
    private readonly api: BackendApiService
  ) {}

  ngOnInit(): void {
    if (!this.gameState.hasActiveGame()) {
      void this.router.navigateByUrl('/auth');
      return;
    }
    if (!this.gameState.lastResult()) {
      void this.router.navigateByUrl('/game');
      return;
    }
    this.api.leaderboard().subscribe({
      next: (rows) => (this.top = rows),
      error: () => (this.top = [])
    });
  }

  nextRound() {
    void this.router.navigateByUrl('/game');
  }

  playAgain() {
    void this.router.navigateByUrl('/home');
  }
}
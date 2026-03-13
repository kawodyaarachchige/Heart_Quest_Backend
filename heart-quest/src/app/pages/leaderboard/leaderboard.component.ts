import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BackendApiService } from '../../services/backend-api.service';
import { GameStateService } from '../../services/game-state.service';
import { LeaderboardEntry } from '../../models/heart.models';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { PanelComponent } from '../../shared/panel/panel.component';

@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [RouterLink, PageHeaderComponent, PanelComponent],
  templateUrl: './leaderboard.component.html',
  styleUrl: './leaderboard.component.scss',
})
export class LeaderboardComponent implements OnInit {
  entries: LeaderboardEntry[] = [];
  loading = true;

  constructor(
    private readonly api: BackendApiService,
    public readonly gameState: GameStateService,
  ) {}

  ngOnInit(): void {
    this.api.leaderboard().subscribe({
      next: (rows) => {
        this.entries = rows;
        this.loading = false;
      },
      error: () => {
        this.entries = [];
        this.loading = false;
      },
    });
  }

  getTopThree(): LeaderboardEntry[] {
    return this.entries.slice(0, 3);
  }

  getRest(): LeaderboardEntry[] {
    return this.entries.slice(3);
  }

  getInitial(username: string): string {
    if (!username || !username.trim()) return '?';
    return username.trim().charAt(0).toUpperCase();
  }

  getMedal(index: number): string {
    switch (index) {
      case 0: return '🥇';
      case 1: return '🥈';
      case 2: return '🥉';
      default: return '';
    }
  }
}

import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BackendApiService } from '../../services/backend-api.service';
import { GameStateService } from '../../services/game-state.service';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.scss',
})
export class PageHeaderComponent {
  constructor(
    private readonly router: Router,
    private readonly api: BackendApiService,
    public readonly gameState: GameStateService,
  ) {}

  getInitial(name: string): string {
    if (!name || !name.trim()) return '?';
    return name.trim().charAt(0).toUpperCase();
  }

  isOnLeaderboard(): boolean {
    return this.router.url.includes('/leaderboard');
  }

  logout(): void {
    this.api.logout().subscribe({
      next: () => this.finishLogout(),
      error: () => this.finishLogout(),
    });
  }

  private finishLogout(): void {
    this.gameState.clearSession();
    void this.router.navigateByUrl('/auth');
  }
}

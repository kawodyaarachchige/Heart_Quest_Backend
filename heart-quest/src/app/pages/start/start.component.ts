import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GameMode, PlayerIdentity } from '../../models/heart.models';
import { BackendApiService } from '../../services/backend-api.service';
import { GameStateService } from '../../services/game-state.service';

@Component({
  selector: 'app-start',
  imports: [FormsModule],
  templateUrl: './start.component.html',
  styleUrl: './start.component.scss'
})
export class StartComponent {
  username = '';
  password = '';
  mode: GameMode = 'timed';
  creatingAccount = false;
  errorMsg: string | null = null;

  constructor(
    private readonly router: Router,
    private readonly api: BackendApiService,
    private readonly gameState: GameStateService
  ) {}

  start() {
    this.errorMsg = null;
    const u = this.username.trim();
    const p = this.password;
    if (!u || !p) {
      this.errorMsg = 'Enter username and password.';
      return;
    }

    const doLogin = () => {
      this.api.login(u, p).subscribe({
        next: (player: PlayerIdentity) => {
          this.api.newGame(this.mode).subscribe({
            next: () => {
              this.gameState.startNewGame(player, this.mode);
              void this.router.navigateByUrl('/game');
            },
            error: () => (this.errorMsg = 'Failed to start a new game.')
          });
        },
        error: (e) => (this.errorMsg = e?.error?.error ?? 'Login failed.')
      });
    };

    if (this.creatingAccount) {
      this.api.register(u, p).subscribe({
        next: () => doLogin(),
        error: (e) => (this.errorMsg = e?.error?.error ?? 'Register failed.')
      });
    } else {
      doLogin();
    }
  }
}

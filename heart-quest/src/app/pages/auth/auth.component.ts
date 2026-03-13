import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GameStateService } from '../../services/game-state.service';
import { BackendApiService } from '../../services/backend-api.service';
import { PlayerIdentity } from '../../models/heart.models';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.scss',
})
export class AuthComponent {
  readonly isLogin = signal(true);

  email = '';
  username = '';
  password = '';
  confirmPassword = '';
  acceptTerms = false;
  hidePassword = true;
  hideSignupPassword = true;
  errorMsg: string | null = null;
  loading = false;

  constructor(
    private readonly router: Router,
    private readonly api: BackendApiService,
    private readonly gameState: GameStateService,
  ) {}

  toggleMode(): void {
    this.isLogin.update((v) => !v);
    this.errorMsg = null;
  }

  togglePassword(): void {
    this.hidePassword = !this.hidePassword;
  }

  toggleSignupPassword(): void {
    this.hideSignupPassword = !this.hideSignupPassword;
  }

  login(): void {
    this.errorMsg = null;
    const emailOrUser = this.email.trim();
    const p = this.password;
    if (!emailOrUser || !p) {
      this.errorMsg = 'Please enter email and password.';
      return;
    }
    this.loading = true;
    this.api.login(emailOrUser, p).subscribe({
      next: (player) => this.onAuthSuccess(player),
      error: (e) => {
        this.loading = false;
        this.errorMsg = e?.error?.error ?? 'Login failed.';
      },
    });
  }

  signup(): void {
    this.errorMsg = null;
    const u = this.username.trim();
    const p = this.password;
    const cp = this.confirmPassword;
    if (!u || !p) {
      this.errorMsg = 'Please enter username and password.';
      return;
    }
    if (p !== cp) {
      this.errorMsg = 'Passwords do not match.';
      return;
    }
    if (!this.acceptTerms) {
      this.errorMsg = 'Please accept the Terms & Conditions.';
      return;
    }
    this.loading = true;
    this.api.register(u, p).subscribe({
      next: () => {
        this.api.login(u, p).subscribe({
          next: (player) => this.onAuthSuccess(player),
          error: (e) => {
            this.loading = false;
            this.errorMsg = e?.error?.error ?? 'Login after signup failed.';
          },
        });
      },
      error: (e) => {
        this.loading = false;
        this.errorMsg = e?.error?.error ?? 'Signup failed.';
      },
    });
  }

  private onAuthSuccess(player: PlayerIdentity): void {
    this.gameState.setPlayer(player);
    this.loading = false;
    void this.router.navigateByUrl('/home');
  }
}

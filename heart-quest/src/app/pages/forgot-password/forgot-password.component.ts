import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ForgotPasswordService } from '../../services/forgot-password.service';

/**
 * Forgot password flow (virtual identity / authentication).
 * Step 1: enter username → Step 2: set new password → Done.
 * High cohesion: only handles forgot-password UI and submit.
 * Low coupling: depends only on ForgotPasswordService and Router.
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  username = '';
  newPassword = '';
  confirmPassword = '';
  hideNewPassword = true;
  hideConfirmPassword = true;
  message: string | null = null;
  isSuccess: boolean | null = null;
  loading = false;
  /** 1 = enter username, 2 = set new password, 3 = done */
  step = 1;

  constructor(
    private readonly forgotPasswordService: ForgotPasswordService,
    private readonly router: Router
  ) {}

  toggleNewPassword(): void {
    this.hideNewPassword = !this.hideNewPassword;
  }

  toggleConfirmPassword(): void {
    this.hideConfirmPassword = !this.hideConfirmPassword;
  }

  continueToStep2(): void {
    const u = this.username.trim();
    if (!u) {
      this.message = 'Please enter your username.';
      this.isSuccess = false;
      return;
    }
    this.message = null;
    this.step = 2;
  }

  resetPassword(): void {
    this.message = null;
    this.loading = true;
    this.forgotPasswordService.resetPassword(this.username, this.newPassword, this.confirmPassword).subscribe({
      next: (result) => {
        this.loading = false;
        this.isSuccess = result.success;
        this.message = result.message;
        if (result.success) {
          this.step = 3;
        }
      },
      error: () => {
        this.loading = false;
        this.isSuccess = false;
        this.message = 'Something went wrong. Please try again.';
      },
    });
  }

  backToLogin(): void {
    void this.router.navigateByUrl('/auth');
  }
}

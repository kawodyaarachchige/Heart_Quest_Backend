import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ForgotPasswordResult } from '../models/heart.models';
import { BackendApiService } from './backend-api.service';

/**
 * Service responsible only for the "forgot password" / request-reset flow.
 * Keeps the component decoupled from HTTP and backend response shape (low coupling).
 * Single responsibility: request a password reset for a given username (high cohesion).
 */
@Injectable({ providedIn: 'root' })
export class ForgotPasswordService {
  constructor(private readonly api: BackendApiService) {}

  /**
   * Request a password reset for the given username.
   * Returns a consistent result shape regardless of backend implementation.
   */
  requestReset(username: string): Observable<ForgotPasswordResult> {
    const trimmed = username?.trim() ?? '';
    if (!trimmed) {
      return of({ success: false, message: 'Please enter your username.' });
    }

    return this.api.requestPasswordReset(trimmed).pipe(
      map((res) => ({
        success: true,
        message: res.message ?? 'If an account exists, you will receive instructions.',
      })),
      catchError((err) => {
        const status = err?.status;
        const bodyMsg = err?.error?.error ?? err?.error?.message;
        if (status === 404 || status === 501) {
          return of({
            success: true,
            message: 'If an account exists, you will receive instructions.',
          });
        }
        return of({
          success: false,
          message: bodyMsg || 'Request failed. Please try again.',
        });
      })
    );
  }

  /**
   * Set a new password for the given username (step 2 of forgot-password).
   */
  resetPassword(username: string, newPassword: string, confirmPassword: string): Observable<ForgotPasswordResult> {
    const trimmed = username?.trim() ?? '';
    if (!trimmed) {
      return of({ success: false, message: 'Username is required.' });
    }
    if (!newPassword || newPassword.length < 4) {
      return of({ success: false, message: 'Password must be at least 4 characters.' });
    }
    if (newPassword !== confirmPassword) {
      return of({ success: false, message: 'Passwords do not match.' });
    }
    return this.api.resetPassword(trimmed, newPassword).pipe(
      map((res) => ({
        success: true,
        message: res.message ?? 'Password has been reset. You can now log in.',
      })),
      catchError((err) => {
        const status = err?.status;
        const bodyMsg = err?.error?.error ?? err?.error?.message;
        if (status === 404) {
          return of({
            success: false,
            message: 'No account found with that username. Use the exact username you use to log in. If you just signed up, try logging in first to confirm it works.',
          });
        }
        return of({
          success: false,
          message: bodyMsg || 'Could not reset password. Please try again.',
        });
      })
    );
  }
}

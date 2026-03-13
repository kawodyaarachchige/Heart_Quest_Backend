import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { GameStateService } from './services/game-state.service';

/**
 * When the backend returns 401 (Not logged in), clear client session and redirect to auth.
 * Skip for /api/auth/me and /api/auth/login so the home and auth components can handle
 * those (show loading/redirect message and stay on auth page for failed login).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const gameState = inject(GameStateService);
  const url = req.url;

  return next(req).pipe(
    catchError((err) => {
      if (err?.status === 401) {
        if (url.includes('/api/auth/me') || url.includes('/api/auth/login') || url.includes('/api/auth/forgot-password') || url.includes('/api/auth/reset-password')) {
          // Let home/auth handle: home shows "Redirecting…", auth shows error message
          return throwError(() => err);
        }
        gameState.clearSession();
        void router.navigateByUrl('/auth');
      }
      return throwError(() => err);
    })
  );
};

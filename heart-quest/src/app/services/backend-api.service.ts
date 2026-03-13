import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameMode, HeartPuzzle, LeaderboardEntry, PlayerIdentity, RoundResult } from '../models/heart.models';

/** Response from POST /api/auth/forgot-password when backend supports it. */
export interface ForgotPasswordApiResponse {
  message?: string;
}

/** Response from GET /api/auth/me when session is valid. */
export interface MeResponse {
  id: number;
  username: string;
  bestScore: number;
  totalGamesPlayed: number;
}

@Injectable({ providedIn: 'root' })
export class BackendApiService {
  constructor(private readonly http: HttpClient) {}

  /**
   * Fetches current session (player + best score). Returns 401 if not logged in.
   * Use withCredentials so session cookie is sent.
   */
  me(): Observable<MeResponse> {
    return this.http.get<MeResponse>('/api/auth/me', { withCredentials: true });
  }

  register(username: string, password: string): Observable<{ ok: true }> {
    return this.http.post<{ ok: true }>('/api/auth/register', { username, password }, { withCredentials: true });
  }

  login(username: string, password: string): Observable<PlayerIdentity> {
    return this.http.post<PlayerIdentity>('/api/auth/login', { username, password }, { withCredentials: true });
  }

  /** Invalidates the server session so the next login gets a clean session. */
  logout(): Observable<{ ok: boolean }> {
    return this.http.post<{ ok: boolean }>('/api/auth/logout', {}, { withCredentials: true });
  }

  /**
   * Request password reset (virtual identity / authentication flow).
   * Backend may return 200 with message, or 404/501 if not implemented.
   */
  requestPasswordReset(username: string): Observable<ForgotPasswordApiResponse> {
    return this.http.post<ForgotPasswordApiResponse>(
      '/api/auth/forgot-password',
      { username: username.trim() },
      { withCredentials: true }
    );
  }

  /**
   * Set new password for an existing account (after forgot-password).
   */
  resetPassword(username: string, newPassword: string): Observable<{ ok: boolean; message?: string }> {
    return this.http.post<{ ok: boolean; message?: string }>(
      '/api/auth/reset-password',
      { username: username.trim(), newPassword },
      { withCredentials: true }
    );
  }

  newGame(mode: GameMode): Observable<any> {
    return this.http.post('/api/game/new', { mode }, { withCredentials: true });
  }

  getPuzzle(): Observable<HeartPuzzle> {
    return this.http.get<HeartPuzzle>('/api/puzzle', { withCredentials: true });
  }

  submit(puzzleId: string, answer: number): Observable<RoundResult> {
    return this.http.post<RoundResult>('/api/round/submit', { puzzleId, answer }, { withCredentials: true });
  }

  skip(puzzleId: string): Observable<RoundResult> {
    return this.http.post<RoundResult>('/api/round/skip', { puzzleId }, { withCredentials: true });
  }

  timeout(puzzleId: string): Observable<RoundResult> {
    return this.http.post<RoundResult>('/api/round/timeout', { puzzleId }, { withCredentials: true });
  }

  leaderboard(): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>('/api/leaderboard', { withCredentials: true });
  }
}


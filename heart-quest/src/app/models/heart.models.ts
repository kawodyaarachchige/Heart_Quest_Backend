export type GameMode = 'timed' | 'practice';

export interface PlayerIdentity {
  id: number;
  username: string;
}

export interface HeartApiResponse {
  question: string;
  solution: number;
  carrots: number;
}

export interface HeartPuzzle {
  puzzleId: string;
  imageDataUrl: string;
  carrots: number;
}

export type RoundEndReason = 'submitted' | 'skipped' | 'timeout';

export interface RoundResult {
  isCorrect: boolean;
  reason: RoundEndReason;
  givenAnswer: number | null;
  solution: number;
  carrots: number;
  scoreDelta: number;
  scoreTotal: number;
  streak: number;
  round: number;
}

export interface LeaderboardEntry {
  username: string;
  bestScore: number;
  updatedAtIso: string;
}

/** Result of a forgot-password / request-reset action (virtual identity flow). */
export interface ForgotPasswordResult {
  success: boolean;
  message: string;
}


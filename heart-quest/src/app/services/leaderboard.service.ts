import { Injectable } from '@angular/core';
import { LeaderboardEntry, PlayerIdentity } from '../models/heart.models';

@Injectable({
  providedIn: 'root'
})
export class LeaderboardService {
  private readonly storageKey = 'hq_leaderboard_v1';

  getTop(limit = 10): LeaderboardEntry[] {
    const items = this.readAll();
    return items
      .slice()
      .sort((a, b) => b.bestScore - a.bestScore || b.updatedAtIso.localeCompare(a.updatedAtIso))
      .slice(0, limit);
  }

  upsertPlayerScore(player: PlayerIdentity, score: number) {
    const items = this.readAll();
    const now = new Date().toISOString();
    const idx = items.findIndex((e) => e.username === player.username);

    const entry: LeaderboardEntry = {
      username: player.username,
      bestScore: score,
      updatedAtIso: now
    };

    if (idx >= 0) {
      items[idx] = entry;
    } else {
      items.push(entry);
    }

    this.writeAll(items);
  }

  private readAll(): LeaderboardEntry[] {
    if (typeof localStorage === 'undefined') return [];
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) return [];
      const parsed = JSON.parse(raw) as unknown;
      if (!Array.isArray(parsed)) return [];
      return parsed.filter(Boolean) as LeaderboardEntry[];
    } catch {
      return [];
    }
  }

  private writeAll(items: LeaderboardEntry[]) {
    if (typeof localStorage === 'undefined') return;
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(items));
    } catch {
      
    }
  }
}

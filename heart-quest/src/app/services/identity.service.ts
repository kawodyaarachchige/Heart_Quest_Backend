import { Injectable } from '@angular/core';
import { PlayerIdentity } from '../models/heart.models';

@Injectable({
  providedIn: 'root'
})
export class IdentityService {
  private readonly nameCookie = 'hq_player_name';
  private readonly idCookie = 'hq_player_id';

  getOrCreate(): PlayerIdentity {
    let id = this.getCookie(this.idCookie);
    if (!id) {
      id = this.newId();
      this.setCookie(this.idCookie, id, 30);
    }

    const name = (this.getCookie(this.nameCookie) ?? '').trim() || 'Guest';
    return { id: parseInt(id), username: name };
  }

  setPlayerName(name: string) {
    const clean = name.trim() || 'Guest';
    this.setCookie(this.nameCookie, clean, 30);
  }

  private newId(): string {
    // Prefer a real UUID; 
    if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
      return crypto.randomUUID();
    }
    return `p_${Math.random().toString(16).slice(2)}_${Date.now().toString(16)}`;
  }

  private getCookie(key: string): string | null {
    if (typeof document === 'undefined') return null;
    const value = document.cookie
      .split('; ')
      .find((row) => row.startsWith(`${encodeURIComponent(key)}=`))
      ?.split('=')[1];
    return value ? decodeURIComponent(value) : null;
  }

  private setCookie(key: string, value: string, days: number) {
    if (typeof document === 'undefined') return;
    const maxAge = Math.floor(days * 24 * 60 * 60);
    document.cookie = `${encodeURIComponent(key)}=${encodeURIComponent(value)}; Max-Age=${maxAge}; Path=/; SameSite=Lax`;
  }
}

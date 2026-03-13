import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {  Observable } from 'rxjs';
import {  HeartPuzzle } from '../models/heart.models';

@Injectable({
  providedIn: 'root'
})
export class HeartApiService {
  // Use dev-server proxy to avoid CORS in browser.
  // proxy.conf.json maps /uob/heart -> http://marcconrad.com/uob/heart
  private readonly baseUrl = '/uob/heart/api.php';

  constructor(private readonly http: HttpClient) {}

  getPuzzle(): Observable<HeartPuzzle> {
    const url = `${this.baseUrl}?out=json&base64=yes`;
    return this.http.get<HeartPuzzle>(url).pipe(
    );
  }
}

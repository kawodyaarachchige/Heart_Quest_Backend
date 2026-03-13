import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BackendApiService } from './services/backend-api.service';
import { GameStateService } from './services/game-state.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  constructor(
    private readonly backendApi: BackendApiService,
    private readonly gameState: GameStateService,
  ) {}

  ngOnInit(): void {
    this.backendApi.me().subscribe({
      next: (res) => {
        this.gameState.setPlayer({ id: res.id, username: res.username });
        this.gameState.setHighestScore(res.bestScore ?? 0);
      },
      error: () => {},
    });
  }
}

import { Routes } from '@angular/router';
import { GameComponent } from './pages/game/game.component';
import { ResultsComponent } from './pages/results/results.component';
import { SplashComponent } from './pages/splash/splash.component';
import { AuthComponent } from './pages/auth/auth.component';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { HomeComponent } from './pages/home/home.component';
import { LeaderboardComponent } from './pages/leaderboard/leaderboard.component';

export const routes: Routes = [
  { path: '', component: SplashComponent, title: 'Heart Quest' },
  { path: 'auth', component: AuthComponent, title: 'Heart Quest — Login' },
  { path: 'auth/forgot-password', component: ForgotPasswordComponent, title: 'Heart Quest — Reset password' },
  { path: 'home', component: HomeComponent, title: 'Heart Quest — Home' },
  { path: 'game', component: GameComponent, title: 'Heart Quest — Game' },
  { path: 'results', component: ResultsComponent, title: 'Heart Quest — Results' },
  { path: 'leaderboard', component: LeaderboardComponent, title: 'Heart Quest — Leaderboard' },
  { path: '**', redirectTo: '' },
];

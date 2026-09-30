import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'private',
    canActivate: [authGuard],
    loadComponent: () => import('./features/private/private.component').then((m) => m.PrivateComponent),
  },
  { path: '', pathMatch: 'full', redirectTo: 'private' },
];

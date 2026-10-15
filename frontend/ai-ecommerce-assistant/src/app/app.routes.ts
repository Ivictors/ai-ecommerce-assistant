import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'products',
    loadComponent: () => import('./features/catalog/product-list.component').then((m) => m.ProductListComponent),
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./features/catalog/product-detail.component').then((m) => m.ProductDetailComponent),
  },
  {
    path: 'private',
    canActivate: [authGuard],
    loadComponent: () => import('./features/private/private.component').then((m) => m.PrivateComponent),
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];

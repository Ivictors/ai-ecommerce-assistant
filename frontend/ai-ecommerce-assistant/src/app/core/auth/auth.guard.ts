import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () =>
  inject(AuthService).isAuthenticated() ? true : inject(Router).createUrlTree(['/']);

import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ApiErrorService } from './api-error.service';
import { Router } from '@angular/router';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const errors = inject(ApiErrorService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        errors.publish(error);
        if (error.status === 401) {
          auth.clearSession();
          void router.navigateByUrl('/login');
        }
      }
      return throwError(() => error);
    }),
  );
};

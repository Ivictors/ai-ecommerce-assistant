import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).getAccessToken();
  if (!token || !isBackendApiRequest(request.url)) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};

function isBackendApiRequest(url: string): boolean {
  try {
    const target = new URL(url, globalThis.location.origin);
    return target.origin === globalThis.location.origin && target.pathname.startsWith('/api/');
  } catch {
    return false;
  }
}

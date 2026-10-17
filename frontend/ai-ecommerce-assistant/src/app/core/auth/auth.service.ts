import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { LoginRequest, LoginResponse, RegistrationRequest, RegistrationResponse } from './auth.model';

const ACCESS_TOKEN_KEY = 'ai-ecommerce.access-token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', credentials).pipe(
      map((response) => {
        if (!response || typeof response.accessToken !== 'string' || !response.accessToken.trim()) {
          throw new Error('Invalid authentication response');
        }
        return response;
      }),
      tap((response) => this.setAccessToken(response.accessToken)),
      catchError((error: unknown) => {
        this.clearSession();
        return throwError(() => error);
      }),
    );
  }

  register(request: RegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>('/api/auth/register', request);
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }

  setAccessToken(token: string): void {
    if (!token.trim()) {
      throw new Error('An access token is required');
    }
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
  }

  clearSession(): void {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return this.getAccessToken() !== null;
  }
}

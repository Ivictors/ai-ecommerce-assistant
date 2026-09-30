import { Injectable } from '@angular/core';

const ACCESS_TOKEN_KEY = 'ai-ecommerce.access-token';

@Injectable({ providedIn: 'root' })
export class AuthService {
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

import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthService, provideRouter([])],
    });
  });

  it('T-003-T4 AC-007 redirects an unauthenticated user to login', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toEqual(TestBed.inject(Router).createUrlTree(['/']));
  });

  it('T-003-T4 AC-003 clears the token on logout', () => {
    const auth = TestBed.inject(AuthService);
    auth.setAccessToken('jwt-value');
    auth.clearSession();

    expect(auth.getAccessToken()).toBeNull();
  });
});

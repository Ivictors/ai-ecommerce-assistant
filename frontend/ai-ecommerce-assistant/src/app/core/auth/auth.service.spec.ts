import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Observable, firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

type LoginRequest = { email: string; password: string };
type LoginResponse = { accessToken: string; tokenType: string; expiresIn: number };

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('T-001-T1 AC-001 logs in and stores only the returned access token', async () => {
    const result = firstValueFrom(login(service, { email: 'user@example.com', password: 'secret' }));

    const request = http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'user@example.com', password: 'secret' });
    request.flush({ accessToken: 'jwt-value', tokenType: 'Bearer', expiresIn: 900 });

    await expect(result).resolves.toEqual({
      accessToken: 'jwt-value',
      tokenType: 'Bearer',
      expiresIn: 900,
    });
    expect(service.getAccessToken()).toBe('jwt-value');
    expect(sessionStorage.length).toBe(1);
  });

  it('T-001-T2 AC-002 does not create a session after authentication failure', async () => {
    const result = firstValueFrom(login(service, { email: 'user@example.com', password: 'wrong' }));
    const request = http.expectOne('/api/auth/login');
    request.flush({ code: 'UNAUTHENTICATED', message: 'Authentication failed' }, { status: 401, statusText: 'Unauthorized' });

    await expect(result).rejects.toBeTruthy();
    expect(service.getAccessToken()).toBeNull();
  });

  it('T-001-T3 AC-006 rejects a successful response without a non-empty access token', async () => {
    const result = firstValueFrom(login(service, { email: 'user@example.com', password: 'secret' }));
    const request = http.expectOne('/api/auth/login');
    request.flush({ accessToken: '', tokenType: 'Bearer', expiresIn: 900 });

    await expect(result).rejects.toBeTruthy();
    expect(service.getAccessToken()).toBeNull();
  });

  it('T-001-T4 AC-008 never stores the submitted password', async () => {
    const result = firstValueFrom(login(service, { email: 'user@example.com', password: 'secret-value' }));
    const request = http.expectOne('/api/auth/login');
    request.flush({ accessToken: 'jwt-value', tokenType: 'Bearer', expiresIn: 900 });
    await result;

    expect(sessionStorage.getItem('ai-ecommerce.access-token')).toBe('jwt-value');
    expect(sessionStorage.getItem('password')).toBeNull();
    expect(Array.from({ length: sessionStorage.length }, (_, index) => sessionStorage.key(index)))
      .not.toContain('secret-value');
  });
});

function login(service: AuthService, request: LoginRequest) {
  return service.login(request) as Observable<LoginResponse>;
}

import { HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { ApiErrorService } from './api-error.service';
import { apiErrorInterceptor } from './api-error.interceptor';
import { AuthService } from '../auth/auth.service';

describe('apiErrorInterceptor', () => {
  let client: HttpClient;
  let http: HttpTestingController;
  let errors: ApiErrorService;
  let auth: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        ApiErrorService,
        provideRouter([]),
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    errors = TestBed.inject(ApiErrorService);
    auth = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => http.verify());

  it('clears stale session and redirects on 401', () => {
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    auth.setAccessToken('stale-token');
    client.get('/api/private').subscribe({ error: () => undefined });
    http
      .expectOne('/api/private')
      .flush({ code: 'UNAUTHENTICATED' }, { status: 401, statusText: 'Unauthorized' });

    expect(auth.getAccessToken()).toBeNull();
    expect(navigate).toHaveBeenCalledWith('/login');
    expect(errors.error()?.code).toBe('UNAUTHENTICATED');
  });

  it('preserves a 403 session and publishes a safe error', () => {
    auth.setAccessToken('token');
    client.get('/api/admin').subscribe({ error: () => undefined });
    http
      .expectOne('/api/admin')
      .flush({ code: 'FORBIDDEN' }, { status: 403, statusText: 'Forbidden' });

    expect(auth.getAccessToken()).toBe('token');
    expect(errors.error()?.message).toContain('not allowed');
  });
});

import { HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let http: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('T-003-T1 AC-004 adds the bearer token to backend API requests', () => {
    auth.setAccessToken('jwt-value');
    httpClient.get('/api/products').subscribe();

    const request = http.expectOne('/api/products');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-value');
    request.flush([]);
  });

  it('T-003-T2 AC-005 does not send the bearer token to an unrelated origin', () => {
    auth.setAccessToken('jwt-value');
    httpClient.get('https://unrelated.example/resource').subscribe();

    const request = http.expectOne('https://unrelated.example/resource');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('T-003-T3 AC-005 does not add a bearer header without a session', () => {
    httpClient.get('/api/products').subscribe();

    const request = http.expectOne('/api/products');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush([]);
  });
});

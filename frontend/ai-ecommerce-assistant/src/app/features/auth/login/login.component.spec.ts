import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('T-002-T1 AC-002 does not submit an invalid form', () => {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('small')?.textContent).toContain('valid email');
  });

  it('T-002-T2 AC-001 navigates to the private shell after login', async () => {
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { email: string; password: string }) => void };
      submit: () => void;
    };
    component.form.setValue({ email: 'user@example.com', password: 'secret' });
    component.submit();
    const request = http.expectOne('/api/auth/login');
    request.flush({ accessToken: 'jwt-value', tokenType: 'Bearer', expiresIn: 900 });
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith('/private');
  });

  it('T-002-T3 AC-002 renders a safe authentication failure', async () => {
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { email: string; password: string }) => void };
      submit: () => void;
    };
    component.form.setValue({ email: 'user@example.com', password: 'wrong' });
    component.submit();
    const request = http.expectOne('/api/auth/login');
    request.flush(
      { code: 'UNAUTHENTICATED', message: 'Authentication failed' },
      { status: 401, statusText: 'Unauthorized' },
    );
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Unable to sign in',
    );
    expect(fixture.nativeElement.textContent).not.toContain('Authentication failed');
  });

  it('T-002-T4 AC-001 exposes an accessible login form', () => {
    expect(fixture.nativeElement.querySelector('label[for="email"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('label[for="password"]')).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('#email[aria-describedby="email-error"]'),
    ).toBeTruthy();
    expect(fixture.nativeElement.querySelector('button[type="submit"]')).toBeTruthy();
  });

  it('disables submission while the login request is pending', () => {
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { email: string; password: string }) => void };
      submit: () => void;
    };
    component.form.setValue({ email: 'user@example.com', password: 'secret' });
    component.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
    http
      .expectOne('/api/auth/login')
      .flush({ accessToken: 'jwt-value', tokenType: 'Bearer', expiresIn: 900 });
  });

  it('keeps provider failure details out of the interface', async () => {
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { email: string; password: string }) => void };
      submit: () => void;
    };
    component.form.setValue({ email: 'user@example.com', password: 'secret' });
    component.submit();
    http
      .expectOne('/api/auth/login')
      .flush({ message: 'provider secret' }, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Unable to sign in',
    );
    expect(fixture.nativeElement.textContent).not.toContain('provider secret');
  });
});

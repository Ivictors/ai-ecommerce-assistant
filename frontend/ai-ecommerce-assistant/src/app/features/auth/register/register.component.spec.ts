import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(RegisterComponent);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('does not submit when passwords differ', () => {
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { name: string; email: string; password: string; passwordConfirmation: string }) => void };
      submit: () => void;
    };
    component.form.setValue({
      name: 'Test User',
      email: 'user@example.com',
      password: 'secret',
      passwordConfirmation: 'different',
    });

    component.submit();
    fixture.detectChanges();

    expect(http.match('/api/auth/register')).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('must match');
  });

  it('registers the user and navigates to login', async () => {
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { name: string; email: string; password: string; passwordConfirmation: string }) => void };
      submit: () => void;
    };
    component.form.setValue({
      name: 'Test User',
      email: 'user@example.com',
      password: 'secret',
      passwordConfirmation: 'secret',
    });

    component.submit();

    const request = http.expectOne('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      name: 'Test User',
      email: 'user@example.com',
      password: 'secret',
      passwordConfirmation: 'secret',
    });
    request.flush({ id: 1, name: 'Test User', email: 'user@example.com', role: 'USER' });
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith('/login');
  });

  it('shows a duplicate email error', async () => {
    const component = fixture.componentInstance as unknown as {
      form: { setValue: (value: { name: string; email: string; password: string; passwordConfirmation: string }) => void };
      submit: () => void;
    };
    component.form.setValue({
      name: 'Test User',
      email: 'user@example.com',
      password: 'secret',
      passwordConfirmation: 'secret',
    });
    component.submit();
    const request = http.expectOne('/api/auth/register');
    request.flush({ code: 'EMAIL_ALREADY_REGISTERED', message: 'Email is already registered' },
      { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('already registered');
  });
});

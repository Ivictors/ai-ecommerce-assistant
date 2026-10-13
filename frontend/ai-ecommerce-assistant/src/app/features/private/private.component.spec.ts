import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { PrivateComponent } from './private.component';

describe('PrivateComponent', () => {
  let fixture: ComponentFixture<PrivateComponent>;
  let auth: AuthService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrivateComponent],
      providers: [AuthService, provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(PrivateComponent);
    auth = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    auth.setAccessToken('jwt-value');
    fixture.detectChanges();
  });

  afterEach(() => sessionStorage.clear());

  it('T-004-T1 AC-003 exposes a logout action', () => {
    expect(fixture.nativeElement.querySelector('[data-testid="logout"]')).toBeTruthy();
  });

  it('T-004-T2 AC-003 clears the session and navigates to login', async () => {
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const logout = fixture.nativeElement.querySelector('[data-testid="logout"]') as HTMLButtonElement;
    logout.click();
    await fixture.whenStable();

    expect(auth.getAccessToken()).toBeNull();
    expect(navigate).toHaveBeenCalledWith('/login');
  });
});

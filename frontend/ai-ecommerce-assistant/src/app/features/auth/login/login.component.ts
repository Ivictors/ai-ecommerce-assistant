import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="auth-page">
      <section class="auth-card" aria-labelledby="login-title">
        <div class="auth-brand" aria-hidden="true">AE</div>
        <p class="eyebrow">AI E-Commerce Assistant</p>
        <h1 id="login-title">Welcome back</h1>
        <p class="auth-intro">Sign in to continue to your personalized shopping assistant.</p>

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <div class="field">
            <label for="email">Email address</label>
            <input id="email" type="email" formControlName="email" autocomplete="username"
              [attr.aria-invalid]="form.controls.email.touched && form.controls.email.invalid"
              aria-describedby="email-error" />
            <small id="email-error" class="field-error" *ngIf="form.controls.email.touched && form.controls.email.invalid">
              Enter a valid email address.
            </small>
          </div>

          <div class="field">
            <label for="password">Password</label>
            <input id="password" type="password" formControlName="password" autocomplete="current-password"
              [attr.aria-invalid]="form.controls.password.touched && form.controls.password.invalid"
              aria-describedby="password-error" />
            <small id="password-error" class="field-error" *ngIf="form.controls.password.touched && form.controls.password.invalid">
              Password is required.
            </small>
          </div>

          <p class="form-error" *ngIf="errorMessage()" role="alert">{{ errorMessage() }}</p>
          <button class="primary-action" type="submit" [disabled]="submitting()">
            {{ submitting() ? 'Signing in...' : 'Sign in' }}
          </button>
          <p class="auth-switch">New here? <a routerLink="/register">Create an account</a></p>
        </form>
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; color: #172033; }
    .auth-page { min-height: 100vh; display: grid; place-items: center; padding: 2rem 1rem; background: linear-gradient(135deg, #eef4ff, #f8fbff 55%, #e8f7f3); }
    .auth-card { width: min(100%, 27rem); padding: 2.5rem; background: #fff; border: 1px solid #dfe7f2; border-radius: 1.25rem; box-shadow: 0 1.25rem 3rem rgb(39 64 97 / 12%); }
    .auth-brand { display: grid; place-items: center; width: 3rem; height: 3rem; margin-bottom: 1rem; border-radius: .9rem; background: #2457d6; color: #fff; font-weight: 800; letter-spacing: .08em; }
    .eyebrow { margin: 0 0 .5rem; color: #2457d6; font-size: .75rem; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }
    h1 { margin: 0; font-size: clamp(1.8rem, 5vw, 2.35rem); letter-spacing: -.04em; }
    .auth-intro { margin: .75rem 0 2rem; color: #5e6b80; line-height: 1.5; }
    form { display: grid; gap: 1.1rem; }
    .field { display: grid; gap: .45rem; }
    label { font-size: .9rem; font-weight: 700; }
    input { width: 100%; box-sizing: border-box; padding: .8rem .9rem; border: 1px solid #b8c4d6; border-radius: .65rem; color: inherit; font: inherit; background: #fff; transition: border-color .15s, box-shadow .15s; }
    input:focus { outline: 3px solid rgb(36 87 214 / 18%); border-color: #2457d6; }
    input[aria-invalid="true"] { border-color: #c0394b; }
    .field-error, .form-error { color: #a32639; font-size: .82rem; line-height: 1.4; }
    .form-error { margin: 0; padding: .75rem; border-radius: .55rem; background: #fff1f2; }
    .primary-action { border: 0; border-radius: .65rem; padding: .85rem 1rem; background: #2457d6; color: #fff; cursor: pointer; font: inherit; font-weight: 700; transition: background .15s, transform .15s; }
    .primary-action:hover:not(:disabled) { background: #1c45ad; transform: translateY(-1px); }
    .primary-action:focus-visible, a:focus-visible { outline: 3px solid rgb(36 87 214 / 35%); outline-offset: 3px; }
    .primary-action:disabled { cursor: wait; opacity: .65; }
    .auth-switch { margin: .25rem 0 0; color: #5e6b80; text-align: center; font-size: .9rem; }
    a { color: #2457d6; font-weight: 700; }
    @media (max-width: 30rem) { .auth-card { padding: 1.5rem; } }
  `],
})
export class LoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.submitting.set(true);
    this.authService.login(this.form.getRawValue()).pipe(
      finalize(() => this.submitting.set(false)),
    ).subscribe({
      next: () => void this.router.navigateByUrl('/private'),
      error: () => this.errorMessage.set('Unable to sign in. Check your credentials and try again.'),
    });
  }
}

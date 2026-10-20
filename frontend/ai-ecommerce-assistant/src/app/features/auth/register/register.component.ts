import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="auth-page">
      <section class="auth-card" aria-labelledby="register-title">
        <div class="auth-brand" aria-hidden="true">AE</div>
        <p class="eyebrow">AI E-Commerce Assistant</p>
        <h1 id="register-title">Create your account</h1>
        <p class="auth-intro">
          Set up your account to keep your conversations and orders in one place.
        </p>

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <div class="field">
            <label for="name">Full name</label>
            <input
              id="name"
              type="text"
              formControlName="name"
              autocomplete="name"
              [attr.aria-invalid]="form.controls.name.touched && form.controls.name.invalid"
              aria-describedby="name-error"
            />
            <small
              id="name-error"
              class="field-error"
              *ngIf="form.controls.name.touched && form.controls.name.invalid"
            >
              Name is required.
            </small>
          </div>

          <div class="field">
            <label for="email">Email address</label>
            <input
              id="email"
              type="email"
              formControlName="email"
              autocomplete="email"
              [attr.aria-invalid]="form.controls.email.touched && form.controls.email.invalid"
              aria-describedby="email-error"
            />
            <small
              id="email-error"
              class="field-error"
              *ngIf="form.controls.email.touched && form.controls.email.invalid"
            >
              Enter a valid email address.
            </small>
          </div>

          <div class="field">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              autocomplete="new-password"
              [attr.aria-invalid]="form.controls.password.touched && form.controls.password.invalid"
              aria-describedby="password-error"
            />
            <small
              id="password-error"
              class="field-error"
              *ngIf="form.controls.password.touched && form.controls.password.invalid"
            >
              Password is required.
            </small>
          </div>

          <div class="field">
            <label for="passwordConfirmation">Confirm password</label>
            <input
              id="passwordConfirmation"
              type="password"
              formControlName="passwordConfirmation"
              autocomplete="new-password"
              [attr.aria-invalid]="
                form.controls.passwordConfirmation.touched &&
                form.controls.passwordConfirmation.invalid
              "
              aria-describedby="password-confirmation-error"
            />
            <small
              id="password-confirmation-error"
              class="field-error"
              *ngIf="
                form.controls.passwordConfirmation.touched &&
                form.controls.passwordConfirmation.invalid
              "
            >
              Password confirmation is required.
            </small>
          </div>

          <p class="form-error" *ngIf="errorMessage()" role="alert">{{ errorMessage() }}</p>
          <button class="primary-action" type="submit" [disabled]="submitting()">
            {{ submitting() ? 'Creating account...' : 'Create account' }}
          </button>
          <p class="auth-switch">Already have an account? <a routerLink="/login">Sign in</a></p>
        </form>
      </section>
    </main>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
        color: #172033;
      }
      .auth-page {
        min-height: 100vh;
        display: grid;
        place-items: center;
        padding: 2rem 1rem;
        background: linear-gradient(135deg, #eef4ff, #f8fbff 55%, #e8f7f3);
      }
      .auth-card {
        width: min(100%, 27rem);
        padding: 2.5rem;
        background: #fff;
        border: 1px solid #dfe7f2;
        border-radius: 1.25rem;
        box-shadow: 0 1.25rem 3rem rgb(39 64 97 / 12%);
      }
      .auth-brand {
        display: grid;
        place-items: center;
        width: 3rem;
        height: 3rem;
        margin-bottom: 1rem;
        border-radius: 0.9rem;
        background: #2457d6;
        color: #fff;
        font-weight: 800;
        letter-spacing: 0.08em;
      }
      .eyebrow {
        margin: 0 0 0.5rem;
        color: #2457d6;
        font-size: 0.75rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
      }
      h1 {
        margin: 0;
        font-size: clamp(1.8rem, 5vw, 2.35rem);
        letter-spacing: -0.04em;
      }
      .auth-intro {
        margin: 0.75rem 0 2rem;
        color: #5e6b80;
        line-height: 1.5;
      }
      form {
        display: grid;
        gap: 1.1rem;
      }
      .field {
        display: grid;
        gap: 0.45rem;
      }
      label {
        font-size: 0.9rem;
        font-weight: 700;
      }
      input {
        width: 100%;
        box-sizing: border-box;
        padding: 0.8rem 0.9rem;
        border: 1px solid #b8c4d6;
        border-radius: 0.65rem;
        color: inherit;
        font: inherit;
        background: #fff;
        transition:
          border-color 0.15s,
          box-shadow 0.15s;
      }
      input:focus {
        outline: 3px solid rgb(36 87 214 / 18%);
        border-color: #2457d6;
      }
      input[aria-invalid='true'] {
        border-color: #c0394b;
      }
      .field-error,
      .form-error {
        color: #a32639;
        font-size: 0.82rem;
        line-height: 1.4;
      }
      .form-error {
        margin: 0;
        padding: 0.75rem;
        border-radius: 0.55rem;
        background: #fff1f2;
      }
      .primary-action {
        border: 0;
        border-radius: 0.65rem;
        padding: 0.85rem 1rem;
        background: #2457d6;
        color: #fff;
        cursor: pointer;
        font: inherit;
        font-weight: 700;
        transition:
          background 0.15s,
          transform 0.15s;
      }
      .primary-action:hover:not(:disabled) {
        background: #1c45ad;
        transform: translateY(-1px);
      }
      .primary-action:focus-visible,
      a:focus-visible {
        outline: 3px solid rgb(36 87 214 / 35%);
        outline-offset: 3px;
      }
      .primary-action:disabled {
        cursor: wait;
        opacity: 0.65;
      }
      .auth-switch {
        margin: 0.25rem 0 0;
        color: #5e6b80;
        text-align: center;
        font-size: 0.9rem;
      }
      a {
        color: #2457d6;
        font-weight: 700;
      }
      @media (max-width: 30rem) {
        .auth-card {
          padding: 1.5rem;
        }
      }
    `,
  ],
})
export class RegisterComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required]],
    passwordConfirmation: ['', [Validators.required]],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (value.password !== value.passwordConfirmation) {
      this.errorMessage.set('Passwords must match.');
      return;
    }

    this.errorMessage.set(null);
    this.submitting.set(true);
    this.authService
      .register(value)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl('/login'),
        error: (error: { status?: number }) => {
          this.errorMessage.set(
            error.status === 409
              ? 'This email is already registered.'
              : 'Unable to create account. Check the submitted data.',
          );
        },
      });
  }
}

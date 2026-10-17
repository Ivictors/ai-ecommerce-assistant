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
    <main>
      <h1>Sign in</h1>
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label for="email">Email</label>
        <input id="email" type="email" formControlName="email" autocomplete="username" />
        <small *ngIf="form.controls.email.touched && form.controls.email.invalid">
          Enter a valid email address.
        </small>

        <label for="password">Password</label>
        <input id="password" type="password" formControlName="password" autocomplete="current-password" />
        <small *ngIf="form.controls.password.touched && form.controls.password.invalid">
          Password is required.
        </small>

        <p *ngIf="errorMessage()" role="alert">{{ errorMessage() }}</p>
        <button type="submit" [disabled]="submitting()">
          {{ submitting() ? 'Signing in…' : 'Sign in' }}
        </button>
        <a routerLink="/register">Create an account</a>
      </form>
    </main>
  `,
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

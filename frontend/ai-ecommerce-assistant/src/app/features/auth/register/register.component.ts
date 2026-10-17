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
    <main>
      <h1>Create account</h1>
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label for="name">Name</label>
        <input id="name" type="text" formControlName="name" autocomplete="name" />
        <small *ngIf="form.controls.name.touched && form.controls.name.invalid">
          Name is required.
        </small>

        <label for="email">Email</label>
        <input id="email" type="email" formControlName="email" autocomplete="email" />
        <small *ngIf="form.controls.email.touched && form.controls.email.invalid">
          Enter a valid email address.
        </small>

        <label for="password">Password</label>
        <input id="password" type="password" formControlName="password" autocomplete="new-password" />
        <small *ngIf="form.controls.password.touched && form.controls.password.invalid">
          Password is required.
        </small>

        <label for="passwordConfirmation">Confirm password</label>
        <input id="passwordConfirmation" type="password" formControlName="passwordConfirmation" autocomplete="new-password" />
        <small *ngIf="form.controls.passwordConfirmation.touched && form.controls.passwordConfirmation.invalid">
          Password confirmation is required.
        </small>

        <p *ngIf="errorMessage()" role="alert">{{ errorMessage() }}</p>
        <button type="submit" [disabled]="submitting()">
          {{ submitting() ? 'Creating account...' : 'Create account' }}
        </button>
        <a routerLink="/login">Back to sign in</a>
      </form>
    </main>
  `,
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
    this.authService.register(value).pipe(
      finalize(() => this.submitting.set(false)),
    ).subscribe({
      next: () => void this.router.navigateByUrl('/login'),
      error: (error: { status?: number }) => {
        this.errorMessage.set(error.status === 409
          ? 'This email is already registered.'
          : 'Unable to create account. Check the submitted data.');
      },
    });
  }
}

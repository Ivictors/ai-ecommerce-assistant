import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-private',
  standalone: true,
  template: `
    <main>
      <h1>AI E-Commerce Assistant</h1>
      <p>This area requires a backend-issued access token.</p>
      <button type="button" data-testid="logout" (click)="logout()">Sign out</button>
    </main>
  `,
})
export class PrivateComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected logout(): void {
    this.authService.clearSession();
    void this.router.navigateByUrl('/login');
  }
}

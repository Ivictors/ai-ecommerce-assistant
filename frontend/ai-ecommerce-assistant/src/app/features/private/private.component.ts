import { Component } from '@angular/core';

@Component({
  selector: 'app-private',
  standalone: true,
  template: `
    <main>
      <h1>AI E-Commerce Assistant</h1>
      <p>This area requires a backend-issued access token.</p>
      <p>The login flow will be connected after issue #35 is implemented.</p>
    </main>
  `,
})
export class PrivateComponent {}

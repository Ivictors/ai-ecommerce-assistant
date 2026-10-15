import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Product } from '../../core/models/product.model';
import { ProductApiService } from '../../core/api/product-api.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main>
      <h1>Products</h1>
      <p *ngIf="loading()">Loading products…</p>
      <p *ngIf="error()" role="alert">Unable to load products.</p>
      <p *ngIf="!loading() && !error() && products().length === 0">No products are available.</p>
      <ul *ngIf="!loading() && !error() && products().length > 0">
        <li *ngFor="let product of products()">
          <a [routerLink]="['/products', product.id]">{{ product.name }}</a>
          <span>{{ product.price | number: '1.2-2' }}</span>
        </li>
      </ul>
    </main>
  `,
})
export class ProductListComponent implements OnInit {
  private readonly api = inject(ProductApiService);
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);

  ngOnInit(): void {
    this.api.findAll().subscribe({
      next: (products) => this.products.set(products),
      error: () => this.error.set(true),
      complete: () => this.loading.set(false),
    });
  }
}

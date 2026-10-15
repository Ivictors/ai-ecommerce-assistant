import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Product } from '../../core/models/product.model';
import { ProductApiService } from '../../core/api/product-api.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main>
      <a routerLink="/products">Back to products</a>
      <p *ngIf="loading()">Loading product…</p>
      <p *ngIf="notFound()" role="alert">Product not found.</p>
      <article *ngIf="product() as item">
        <h1>{{ item.name }}</h1>
        <p>{{ item.description }}</p>
        <p>Price: {{ item.price | number: '1.2-2' }}</p>
        <p>Stock: {{ item.stock }}</p>
      </article>
    </main>
  `,
})
export class ProductDetailComponent implements OnInit {
  private readonly api = inject(ProductApiService);
  private readonly route = inject(ActivatedRoute);
  protected readonly product = signal<Product | null>(null);
  protected readonly loading = signal(true);
  protected readonly notFound = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.loading.set(false);
      this.notFound.set(true);
      return;
    }
    this.api.findById(id).subscribe({
      next: (product) => this.product.set(product),
      error: (error: { status?: number }) => this.notFound.set(error.status === 404),
      complete: () => this.loading.set(false),
    });
  }
}

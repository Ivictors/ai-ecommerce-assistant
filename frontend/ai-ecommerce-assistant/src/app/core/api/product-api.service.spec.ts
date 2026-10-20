import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductApiService } from './product-api.service';

describe('ProductApiService', () => {
  let service: ProductApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductApiService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the catalog from the backend', () => {
    service.findAll().subscribe((products) => expect(products).toHaveLength(1));
    const request = http.expectOne('/api/products');
    request.flush([
      { id: 1, name: 'Coffee', description: 'Fresh', price: 10, stock: 2, active: true },
    ]);
  });

  it('loads a product detail by backend-issued id', () => {
    service.findById(7).subscribe((product) => expect(product.id).toBe(7));
    const request = http.expectOne('/api/products/7');
    request.flush({
      id: 7,
      name: 'Coffee',
      description: 'Fresh',
      price: 10,
      stock: 2,
      active: true,
    });
  });
});

import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ProductListComponent } from './product-list.component';

describe('ProductListComponent', () => {
  let fixture: ComponentFixture<ProductListComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(ProductListComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('renders backend-owned product values', () => {
    http.expectOne('/api/products').flush([{ id: 1, name: 'Coffee', description: 'Fresh', price: 10, stock: 2, active: true }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Coffee');
    expect(fixture.nativeElement.textContent).toContain('10.00');
  });

  it('renders an empty state', () => {
    http.expectOne('/api/products').flush([]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No products are available');
  });
});

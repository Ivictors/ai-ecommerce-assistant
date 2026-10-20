import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CustomerComponent } from './customer.component';

describe('CustomerComponent', () => {
  let fixture: ComponentFixture<CustomerComponent>;
  let http: HttpTestingController;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(CustomerComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });
  afterEach(() => http.verify());
  it('loads own orders and conversations', () => {
    http
      .expectOne('/api/orders')
      .flush([{ id: 1, status: 'CREATED', total: 10, createdAt: '2026-01-01' }]);
    http.expectOne('/api/conversations').flush([{ id: 7, createdAt: '2026-01-01' }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Order #1');
    expect(fixture.nativeElement.textContent).toContain('Conversation #7');
  });
  it('uses a backend-issued conversation id after creation', () => {
    http.expectOne('/api/orders').flush([]);
    http.expectOne('/api/conversations').flush([]);
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const create = buttons.find((item) => item.textContent?.includes('Start')) as HTMLButtonElement;
    create.click();
    const request = http.expectOne('/api/conversations');
    request.flush({ id: 9, createdAt: '2026-01-01' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Selected conversation: 9');
  });
});

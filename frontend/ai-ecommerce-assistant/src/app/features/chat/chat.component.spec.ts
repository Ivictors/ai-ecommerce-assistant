import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { ChatComponent } from './chat.component';

describe('ChatComponent', () => {
  let fixture: ComponentFixture<ChatComponent>;
  let http: HttpTestingController;
  beforeEach(async () => { await TestBed.configureTestingModule({ imports: [ChatComponent], providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '7' } } } }] }).compileComponents(); fixture = TestBed.createComponent(ChatComponent); http = TestBed.inject(HttpTestingController); fixture.detectChanges(); });
  afterEach(() => http.verify());
  it('renders all approved structured response variants without parsing text', () => {
    const types = [
      { type: 'TEXT', data: null, message: 'plain' },
      { type: 'PRODUCT_INFORMATION', data: { id: 1, name: 'Coffee', price: 10, description: 'x', stock: 2, active: true }, message: 'product' },
      { type: 'ORDER_STATUS', data: { id: 2, status: 'PAID', total: 10, createdAt: '2026-01-01' }, message: 'order' },
      { type: 'POLICY_INFORMATION', data: { content: 'Policy' }, message: 'policy' },
      { type: 'FALLBACK', data: null, message: 'fallback' },
    ];
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = 'hello'; input.dispatchEvent(new Event('input')); fixture.detectChanges();
    (fixture.componentInstance as unknown as { send: (message: string) => void }).send('hello');
    http.expectOne('/api/chat?conversationId=7').flush(types[0]);
    for (const response of types.slice(1)) { (fixture.componentInstance as unknown as { send: (message: string) => void }).send('hello'); http.expectOne('/api/chat?conversationId=7').flush({ ...response, schemaVersion: '1' }); }
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-testid="product-response"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-testid="order-response"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-testid="policy-response"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[data-testid="fallback-response"]')).toBeTruthy();
  });

  it('does not call the backend for a null or invalid conversation id', async () => {
    const route = TestBed.inject(ActivatedRoute) as unknown as { snapshot: { paramMap: { get: () => string } } };
    route.snapshot.paramMap.get = () => '0';
    const invalid = TestBed.createComponent(ChatComponent);
    invalid.detectChanges();
    expect(invalid.nativeElement.textContent).toContain('valid conversation');
  });
});

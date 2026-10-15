import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ConversationApiService } from './conversation-api.service';

describe('ConversationApiService', () => {
  let service: ConversationApiService;
  let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [ConversationApiService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(ConversationApiService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('lists owned conversations', () => { service.findMine().subscribe(); expect(http.expectOne('/api/conversations').request.method).toBe('GET'); });
  it('creates a conversation without a client owner id', () => { service.create().subscribe(); const request = http.expectOne('/api/conversations'); expect(request.request.method).toBe('POST'); expect(request.request.body).toBeNull(); request.flush({ id: 1, createdAt: '2026-01-01' }); });
});

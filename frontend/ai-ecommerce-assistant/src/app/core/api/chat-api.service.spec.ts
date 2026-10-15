import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ChatApiService } from './chat-api.service';

describe('ChatApiService', () => {
  let service: ChatApiService;
  let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [ChatApiService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(ChatApiService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('requires a positive backend-issued conversation id', () => { service.send(0, 'hello').subscribe({ error: (error) => expect(error.message).toContain('conversationId') }); expect(() => http.expectNone('/api/chat')).not.toThrow(); });
  it('sends the message with the conversation id', () => { service.send(7, 'hello').subscribe(); const request = http.expectOne('/api/chat?conversationId=7'); expect(request.request.body).toBe('hello'); request.flush({ type: 'TEXT', schemaVersion: '1', message: 'Hi', data: null }); });
});

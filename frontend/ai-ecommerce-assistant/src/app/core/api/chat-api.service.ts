import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatResponse } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatApiService {
  private readonly http = inject(HttpClient);
  send(conversationId: number, message: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>('/api/chat', message, {
      params: new HttpParams().set('conversationId', conversationId),
      headers: { 'Content-Type': 'text/plain' },
    });
  }
}

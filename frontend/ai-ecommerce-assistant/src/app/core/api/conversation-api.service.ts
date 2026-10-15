import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Conversation } from '../models/conversation.model';

@Injectable({ providedIn: 'root' })
export class ConversationApiService {
  private readonly http = inject(HttpClient);
  findMine(): Observable<Conversation[]> { return this.http.get<Conversation[]>('/api/conversations'); }
  create(): Observable<Conversation> { return this.http.post<Conversation>('/api/conversations', null); }
}

import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ChatApiService } from '../../core/api/chat-api.service';
import { ChatResponse, ProductInformationData, OrderStatusData, PolicyInformationData } from '../../core/models/chat.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main>
      <a routerLink="/account">Back to customer area</a>
      <h1>Assistant</h1>
      <p *ngIf="invalidConversation()" role="alert">A valid conversation is required.</p>
      <section *ngFor="let response of responses()" class="response">
        <p>{{ response.message }}</p>
        <ng-container [ngSwitch]="response.type">
          <article *ngSwitchCase="'PRODUCT_INFORMATION'" data-testid="product-response">
            <strong>{{ product(response)?.name }}</strong>
            <span>{{ product(response)?.price | number: '1.2-2' }}</span>
          </article>
          <article *ngSwitchCase="'ORDER_STATUS'" data-testid="order-response">
            <strong>Order #{{ order(response)?.id }}</strong>
            <span>{{ order(response)?.status }}</span>
          </article>
          <article *ngSwitchCase="'POLICY_INFORMATION'" data-testid="policy-response">
            <span>{{ policy(response)?.content }}</span>
          </article>
          <span *ngSwitchCase="'FALLBACK'" data-testid="fallback-response">More information is needed.</span>
          <span *ngSwitchCase="'TEXT'" data-testid="text-response">{{ response.message }}</span>
          <span *ngSwitchDefault data-testid="unsupported-response">This response version is not supported.</span>
        </ng-container>
      </section>
      <p *ngIf="error()" role="alert">Unable to send the message.</p>
      <button *ngIf="lastMessage()" type="button" (click)="send(lastMessage()!)">Retry</button>
      <form *ngIf="!invalidConversation()" (ngSubmit)="send(message)">
        <label for="message">Message</label>
        <input id="message" name="message" [(ngModel)]="message" required />
        <button type="submit" [disabled]="loading() || !message.trim()">Send</button>
      </form>
    </main>
  `,
})
export class ChatComponent implements OnInit {
  private readonly api = inject(ChatApiService);
  private readonly route = inject(ActivatedRoute);
  protected readonly responses = signal<ChatResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);
  protected readonly invalidConversation = signal(false);
  protected readonly lastMessage = signal<string | null>(null);
  protected conversationId: number | null = null;
  protected message = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('conversationId'));
    if (!Number.isInteger(id) || id <= 0) this.invalidConversation.set(true);
    else this.conversationId = id;
  }

  protected send(message: string): void {
    if (!this.conversationId || !message.trim() || this.loading()) return;
    this.loading.set(true);
    this.error.set(false);
    this.lastMessage.set(message);
    this.api.send(this.conversationId, message).subscribe({
      next: (response) => {
        if (response.schemaVersion !== '1') this.error.set(true);
        else this.responses.update((items) => [...items, response]);
        this.message = '';
        this.loading.set(false);
      },
      error: () => { this.error.set(true); this.loading.set(false); },
    });
  }

  protected product(response: ChatResponse): ProductInformationData | null { return response.type === 'PRODUCT_INFORMATION' ? response.data as ProductInformationData : null; }
  protected order(response: ChatResponse): OrderStatusData | null { return response.type === 'ORDER_STATUS' ? response.data as OrderStatusData : null; }
  protected policy(response: ChatResponse): PolicyInformationData | null { return response.type === 'POLICY_INFORMATION' ? response.data as PolicyInformationData : null; }
}

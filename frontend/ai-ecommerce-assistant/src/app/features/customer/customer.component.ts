import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ConversationApiService } from '../../core/api/conversation-api.service';
import { OrderApiService } from '../../core/api/order-api.service';
import { Conversation } from '../../core/models/conversation.model';
import { Order } from '../../core/models/order.model';

@Component({
  selector: 'app-customer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main>
      <h1>Customer area</h1>
      <section>
        <h2>Orders</h2>
        <p *ngIf="ordersError()" role="alert">Unable to load orders.</p>
        <p *ngIf="!ordersError() && orders().length === 0">No orders found.</p>
        <ul><li *ngFor="let order of orders()">Order #{{ order.id }} — {{ order.status }} — {{ order.total }}</li></ul>
      </section>
      <section>
        <h2>Conversations</h2>
        <p *ngIf="conversationError()" role="alert">Unable to load conversations.</p>
        <button type="button" (click)="createConversation()" [disabled]="creating()">Start conversation</button>
        <ul>
          <li *ngFor="let conversation of conversations()">
            <button type="button" (click)="selectConversation(conversation.id)">Conversation #{{ conversation.id }}</button>
          </li>
        </ul>
        <p *ngIf="selectedConversationId() as id">Selected conversation: {{ id }}</p>
        <a *ngIf="selectedConversationId() as id" [routerLink]="['/chat', id]">Open chat</a>
      </section>
    </main>
  `,
})
export class CustomerComponent implements OnInit {
  private readonly ordersApi = inject(OrderApiService);
  private readonly conversationsApi = inject(ConversationApiService);
  protected readonly orders = signal<Order[]>([]);
  protected readonly conversations = signal<Conversation[]>([]);
  protected readonly selectedConversationId = signal<number | null>(null);
  protected readonly creating = signal(false);
  protected readonly ordersError = signal(false);
  protected readonly conversationError = signal(false);

  ngOnInit(): void {
    this.ordersApi.findMine().subscribe({ next: (items) => this.orders.set(items), error: () => this.ordersError.set(true) });
    this.conversationsApi.findMine().subscribe({ next: (items) => this.conversations.set(items), error: () => this.conversationError.set(true) });
  }

  protected selectConversation(id: number): void {
    if (Number.isInteger(id) && id > 0) this.selectedConversationId.set(id);
  }

  protected createConversation(): void {
    this.creating.set(true);
    this.conversationsApi.create().subscribe({
      next: (conversation) => {
        this.conversations.update((items) => [...items, conversation]);
        this.selectedConversationId.set(conversation.id);
        this.creating.set(false);
      },
      error: () => { this.conversationError.set(true); this.creating.set(false); },
    });
  }
}

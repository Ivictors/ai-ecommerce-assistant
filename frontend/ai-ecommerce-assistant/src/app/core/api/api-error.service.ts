import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { ApiError } from '../models/api-error.model';

export interface ApiErrorState {
  status: number;
  code: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ApiErrorService {
  private readonly currentState = signal<ApiErrorState | null>(null);
  readonly error = this.currentState.asReadonly();

  publish(response: HttpErrorResponse): void {
    const body = response.error as Partial<ApiError> | null;
    this.currentState.set({
      status: response.status,
      code: typeof body?.code === 'string' ? body.code : `HTTP_${response.status || 0}`,
      message: this.safeMessage(response.status),
    });
  }

  clear(): void {
    this.currentState.set(null);
  }

  private safeMessage(status: number): string {
    return (
      {
        401: 'Your session has expired. Please sign in again.',
        403: 'You are not allowed to perform this action.',
        404: 'The requested resource was not found.',
        409: 'The operation conflicts with the current state.',
        422: 'The submitted data is invalid.',
        500: 'The service could not complete the request.',
      }[status] ?? 'The request could not be completed.'
    );
  }
}

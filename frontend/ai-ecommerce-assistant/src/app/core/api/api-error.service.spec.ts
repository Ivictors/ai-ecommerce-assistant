import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ApiErrorService } from './api-error.service';

describe('ApiErrorService', () => {
  let service: ApiErrorService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ApiErrorService] });
    service = TestBed.inject(ApiErrorService);
  });

  for (const [status, code, message] of [
    [403, 'FORBIDDEN', 'You are not allowed to perform this action.'],
    [404, 'RESOURCE_NOT_FOUND', 'The requested resource was not found.'],
    [409, 'CONFLICT', 'The operation conflicts with the current state.'],
    [422, 'INVALID_INPUT', 'The submitted data is invalid.'],
    [500, 'INTERNAL_ERROR', 'The service could not complete the request.'],
  ] as const) {
    it(`maps ${status} to a safe message while preserving the backend code`, () => {
      service.publish(new HttpErrorResponse({ status, error: { code } }));
      expect(service.error()).toEqual({ status, code, message });
    });
  }

  it('does not expose backend detail text as the user message', () => {
    service.publish(new HttpErrorResponse({ status: 500, error: { code: 'INTERNAL_ERROR', message: 'SQL secret' } }));
    expect(service.error()?.message).not.toContain('SQL secret');
  });
});

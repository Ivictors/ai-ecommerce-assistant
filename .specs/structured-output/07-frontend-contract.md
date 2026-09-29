# Frontend Contract — Structured Chat Responses

The future Angular client consumes `POST /api/chat` as JSON.

Common fields:

```typescript
type ChatResponse = {
  type: 'TEXT' | 'PRODUCT_INFORMATION' | 'ORDER_STATUS' | 'POLICY_INFORMATION' | 'FALLBACK';
  schemaVersion: string;
  message: string;
  data: unknown | null;
};
```

The client must branch on `type` and must not parse `message` to obtain
authoritative business values. Product and order fields are display data from
the backend; they are not authorization decisions.

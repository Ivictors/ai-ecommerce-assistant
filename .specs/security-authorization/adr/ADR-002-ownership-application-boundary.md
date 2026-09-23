# ADR-002: Enforçar ownership nos casos de uso da aplicação

## Status
Accepted

## Context
Anotações REST protegem a entrada, mas não garantem que um recurso carregado pertença ao usuário autenticado nem protegem chamadas internas/Tools.

## Decision
Ownership será validado em application services/use cases, usando a identidade autenticada e consultas filtradas por proprietário quando possível.

## Consequences
O controle permanece aplicável a REST e IA, mas exige que cada caso de uso declare explicitamente seu contexto de usuário.

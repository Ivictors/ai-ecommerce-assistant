# ADR-004: Identificar administrador pelo claim de role verificado

## Status
Accepted

## Context
A primeira milestone precisa proteger mutações de catálogo já existentes sem criar um novo modelo de administração não aprovado.

## Decision
O administrador será identificado pelo claim verificado `role` com valor `ADMIN`. O backend nunca aceitará role do corpo, query ou frontend.

## Consequences
A configuração do provedor JWT precisa emitir e assinar esse claim; mudanças futuras para role persistida exigirão nova decisão e revisão.

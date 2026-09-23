# ADR-003: Retornar 404 para recursos privados de outro usuário

## Status
Accepted

## Context
Um `403` pode revelar que um identificador de recurso existe mesmo quando o cliente não possui acesso.

## Decision
Pedidos, conversas e outros recursos privados de outro cliente serão tratados como não encontrados e retornarão `404 Not Found`.

## Consequences
Há menor vazamento de existência de recursos, mas clientes não distinguem recurso inexistente de recurso pertencente a outro usuário.

# ADR-001: Usar o JWT `sub` como identidade numérica do usuário

## Status
Accepted

## Context
O backend precisa associar a requisição autenticada ao `User` persistido sem confiar em identificadores enviados pelo cliente.

## Decision
O claim verificado `sub` conterá o identificador numérico do `User` atual.

## Consequences
O mapeamento é direto e evita lookup por dados mutáveis, mas tokens com `sub` inválido devem ser rejeitados e a configuração do provedor precisa manter esse contrato.

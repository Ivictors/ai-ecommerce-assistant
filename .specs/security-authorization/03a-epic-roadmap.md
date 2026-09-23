# Roadmap Epic — Autenticação e Autorização

## Slices

### S-001 — Identidade autenticada

- Escopo: resolver `SecurityIdentity`, validar `sub` numérico e representar usuário anônimo/autenticado.
- AC focus: AC-001, AC-002, AC-003.
- Dependências: configuração JWT existente.
- Intenção: estabelecer uma fonte única e confiável para o usuário atual.

### S-002 — Matriz de endpoint e administrador

- Escopo: aplicar a matriz pública, cliente e `role=ADMIN` aos endpoints atuais.
- AC focus: AC-009, AC-010, AC-011, AC-014.
- Dependências: S-001; contrato de erros da issue #5.
- Intenção: proteger a fronteira REST sem confiar no frontend.

### S-003 — Ownership de pedidos e conversas

- Escopo: remover confiança em `userId` fornecido pelo cliente e garantir acesso somente aos recursos próprios.
- AC focus: AC-004, AC-005, AC-006, AC-007, AC-008.
- Dependências: S-001; S-002.
- Intenção: fechar o risco de acesso cruzado entre clientes.

### S-004 — Autorização de Tools e fluxos de IA

- Escopo: garantir que Tools usem application services e preservem identidade/ownership.
- AC focus: AC-012, AC-013, AC-014.
- Dependências: S-001; S-003.
- Intenção: impedir bypass de segurança pelo LLM.

### S-005 — Validação integrada e observabilidade de segurança

- Escopo: testes REST/integrados, respostas de erro, logs seguros e validação do conjunto.
- AC focus: AC-015 e todos os ACs anteriores.
- Dependências: S-001 a S-004; issue #5.
- Intenção: demonstrar que a segurança funciona na fronteira real da aplicação.

## Dependency graph

```text
S-001
  |
  +--> S-002 ----+
  |              |
  +--> S-003 ----+--> S-005
         |
         +--> S-004 -+
```

## Milestone mapping

- Milestone M1: S-001 e S-002 — identidade e endpoints.
- Milestone M2: S-003 — ownership de dados privados.
- Milestone M3: S-004 — IA segura.
- Milestone M4: S-005 — validação e gate de conclusão.

## Exit criteria

- Todos os ACs possuem tarefas e testes rastreáveis.
- Testes unitários e REST/security passam.
- Nenhum endpoint da matriz permanece sem proteção correspondente.
- Nenhuma Tool acessa repositório ou banco diretamente.
- O contrato de erro está aprovado e utilizado.

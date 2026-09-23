# Revisão da Especificação — Autenticação e Autorização

Spec revisada: `.specs/security-authorization/01-spec.md`
Data: 2026-09-23

## Checklist

### Goal clarity

- [PASS] O objetivo está escrito em termos visíveis para o usuário.
- [PASS] O objetivo está em um único parágrafo.
- [PASS] Um novo membro consegue entender o propósito da funcionalidade.

### Acceptance criteria quality

- [PASS] Todos os ACs usam uma forma EARS-lite válida.
- [PASS] Todos os ACs possuem IDs estáveis AC-NNN.
- [PASS] Os ACs são suficientemente atômicos para orientar testes.
- [PASS] Os ACs são verificáveis por comportamento observável.
- [PASS] Não há nomes de classes, métodos ou bibliotecas nos ACs.
- [PASS] Não há AC puramente subjetivo.
- [PASS] Há caminhos de sucesso, falha de autenticação/autorização e edge cases de ownership.
- [PASS] Não foram identificados ACs duplicados; há sobreposição intencional entre regras gerais e específicas de ownership.

### Non-goals

- [PASS] Os non-goals são explícitos e específicos.
- [PASS] Os non-goals cobrem login, JWT provider, OAuth, MFA, pagamentos, inventário e promoções.

### Non-functional requirements

- [PASS] Não há NFRs quantitativos aprovados para esta milestone; as restrições de segurança e privacidade foram movidas para uma seção própria e possuem método de verificação por testes/revisão.

### Glossary

- [PASS] Os termos principais de autenticação, autorização, ownership e AI Tool estão definidos.
- [PASS] As definições não dependem de termos indefinidos relevantes.

### Source

- [PASS] A issue #2 e sua URL estão registradas.
- [PASS] A data de snapshot está registrada.

### Open questions

- [PASS] Não há perguntas com status `open`.
- [PASS] As resoluções Q-001 a Q-006 registram as respostas originais do usuário e uma interpretação explícita quando necessário.

### Completeness

- [PASS] O comportamento para credencial inválida, expirada e usuário anônimo está coberto.
- [PASS] O comportamento de sucesso para cliente e administrador está coberto.
- [PASS] O comportamento obrigatório e os limites da primeira milestone estão identificados.
- [PASS] AC-015 declara a dependência do contrato de erros HTTP; a issue #5 permanece uma dependência de implementação e deverá estar aprovada antes do código final.
- [PASS] Não há linguagem especulativa sem Q-NNN ou non-goal.

## Summary of findings

### Must-fix

Nenhum.

### Should-fix

- Indicar quais ACs serão validados por testes unitários, testes REST e testes de integração durante o design técnico.

### Nit

- Padronizar o idioma dos ACs; atualmente eles estão em inglês enquanto o restante da especificação está em português.

## Verdict

**PASS** — a especificação está pronta para o design técnico.

## Required next action

Seguir para o design técnico SLDD-02, mantendo a issue #5 como dependência para o contrato final de erros HTTP.

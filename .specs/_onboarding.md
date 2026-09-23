# Onboarding do Código Existente

Data do levantamento: 2026-09-23
Escopo: repositório `ai-ecommerce-assistant`, com foco atual no backend em `backend/ai-ecommerce-assistant/`.

## 1. Classificação e estrutura

- Tipo: brownfield; já existem código, banco, migrações, testes e commits de funcionalidade.
- Forma atual: repositório único com backend Quarkus e arquivos de infraestrutura no diretório raiz.
- Backend: aplicação Java 21/Quarkus, empacotada com Maven Wrapper.
- Frontend: previsto nos documentos e no roadmap, mas não há `frontend/package.json` nem código Angular no estado atual.
- Banco: PostgreSQL com imagem `pgvector/pgvector:pg16` no `docker-compose.yml`.
- Migrações: Flyway em `backend/ai-ecommerce-assistant/src/main/resources/db/migration/`.
- Entrada de desenvolvimento: `backend/ai-ecommerce-assistant/mvnw.cmd quarkus:dev` no Windows.
- Validação principal: `backend/ai-ecommerce-assistant/mvnw.cmd test`.

Estrutura relevante:

```text
AGENTS.md
specs/
.specs/
backend/ai-ecommerce-assistant/
  pom.xml
  src/main/java/com/victor/ecommerce/
    domain/
    application/
    infrastructure/
    presentation/rest/
  src/main/resources/db/migration/
  src/test/java/com/victor/ecommerce/
docker-compose.yml
README.md
```

## 2. Arquitetura atual

O código segue, de forma parcial, a separação definida em `AGENTS.md`:

- `domain`: entidades e estados de negócio, como `Order`, `Product`, `Conversation` e `User`.
- `application`: coordenação de casos de uso, como `OrderService`, `ConversationService`, `ChatApplicationService` e `CurrentUserService`.
- `infrastructure`: persistência Panache, integração LangChain4j/OpenAI e Tools.
- `presentation/rest`: recursos HTTP, DTOs e validação/controle de acesso de transporte.

Fluxos observados:

- Pedido: REST → aplicação → repositório, com filtro por usuário autenticado em `OrderService`.
- Chat: REST → `ChatApplicationService` → `EcommerceAssistant`; o recurso REST duplicado na infraestrutura foi removido.
- IA: `EcommerceAssistant` usa memória LangChain4j e Tools de produto/pedido.
- Persistência: entidades JPA/Panache e repositórios em `infrastructure/persistence`.
- Segurança: SmallRye JWT está configurado como dependência; `CurrentUserService` lê o principal como identificador numérico.

## 3. Convenções a preservar

- Java 21 e quatro espaços de indentação.
- Uma classe pública por arquivo.
- Nomes `*Resource`, `*Service`, `*Repository`, `*Request` e `*Response`.
- Recursos REST finos, delegando regras para a aplicação/domínio.
- Testes JUnit 5 com Mockito, organizados por pacote correspondente.
- Migrações Flyway versionadas; não alterar migrações já aplicadas.
- Não confiar em frontend ou LLM para preço, estoque, pagamento, autorização ou ownership.
- Não expor entidades de persistência diretamente como contrato público quando DTOs forem necessários.

## 4. Integrações e pontos de entrada

- PostgreSQL/PGVector via Docker Compose.
- Flyway executado no início da aplicação conforme configuração de exemplo.
- LangChain4j/OpenAI via `quarkus-langchain4j-openai`.
- JWT via `quarkus-smallrye-jwt`.
- REST via Quarkus REST/Jackson.
- Persistência via Hibernate ORM Panache.
- Não há mensageria, CI/CD ou frontend implementado identificados neste levantamento.

## 5. Baseline de qualidade

Comando executado em 2026-09-23:

```text
backend/ai-ecommerce-assistant/.\mvnw.cmd test
```

Resultado: 10 testes executados, 10 aprovados, 0 falhas, 0 erros.

O build exibiu avisos do Mockito/Byte Buddy sobre self-attach de agente Java. Eles não falharam o build, mas devem ser acompanhados quando o JDK restringir dynamic agent loading.

Não foram encontrados comandos ou configurações de lint, formatter, cobertura, SpotBugs, Checkstyle ou pipeline GitHub Actions.

## 6. Riscos, dívidas e desconhecidos

- Autenticação/autorização ainda não está completa para chat, conversas e operações administrativas.
- O contrato de resolução do principal JWT para `User` ainda precisa ser especificado e validado.
- `ConversationResource` expõe consulta por `userId` informado na URL; ownership deve ser reforçado pelo usuário autenticado.
- `ProductResource` ainda possui operações que precisam de decisão e proteção administrativa.
- O contrato padronizado de erros HTTP ainda não existe.
- O comportamento de criação de conversa e persistência de mensagens ainda não está definido.
- A implementação atual usa entidades JPA também como parte do domínio; qualquer refatoração deve ser precedida por decisão arquitetural.
- A configuração de exemplo contém placeholders e precisa permanecer sem segredos reais.
- Não há testes REST de integração nem ambiente automatizado de banco/Testcontainers identificado.
- Não há cobertura de código mensurada.
- O frontend Angular está planejado, mas ausente no estado atual.
- O README ainda é o template inicial do Quarkus e não descreve o domínio, segurança, Docker ou fluxo SLDD.
- A remoção de `.env-example` continua como alteração pré-existente fora do commit de onboarding.

## 7. Lacunas para o fluxo SLDD

- [ ] Criar artefatos `.specs/<feature-id>/` para cada feature.
- [ ] Definir baseline de integração com banco e decidir sobre Testcontainers.
- [ ] Adicionar lint/format/quality gates somente após decisão do projeto.
- [ ] Adicionar CI que execute os comandos aprovados.
- [ ] Criar matriz de rastreabilidade entre requisitos, tarefas, testes e código.
- [ ] Formalizar contrato de erros, segurança e ownership antes de RAG.

## 8. Contexto para as próximas etapas

A próxima unidade de trabalho é a conclusão da autenticação e autorização. Ela deve começar por uma especificação de intenção e critérios de aceitação, passar por revisão e design, e só então gerar testes e implementação. O baseline atual está verde para testes unitários existentes, mas não representa ainda uma validação completa de segurança, banco, REST ou integração com OpenAI.

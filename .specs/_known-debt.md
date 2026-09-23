# Known Technical Debt

- [ ] Complete authentication and authorization for chat, conversations, and administrative operations.
- [ ] Define how the JWT principal resolves to the persisted user.
- [ ] Enforce conversation ownership and stop trusting client-provided `userId`.
- [ ] Define and implement a standardized HTTP error contract.
- [ ] Define conversation creation and message persistence.
- [ ] Add REST integration tests and decide whether to use Testcontainers.
- [ ] Configure test coverage if the project approves a threshold.
- [ ] Evaluate linting, formatting, and static analysis.
- [ ] Create a CI pipeline for approved tests and quality gates.
- [ ] Review the Mockito/Byte Buddy dynamic-agent warning.
- [ ] Evaluate separating JPA entities from the domain model before adding complexity.
- [ ] Implement the Angular frontend, which is currently absent.
- [ ] Update the README with architecture, execution, security, Docker, and SLDD workflow.
- [ ] Define RAG, document ingestion, permissions, and separation between knowledge and transactional state.

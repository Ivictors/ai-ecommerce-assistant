# MVP Quarkus LTS Validation

The Quarkus platform and Quarkus LangChain4j BOMs are aligned to Quarkus
3.40.1, the latest community micro release of the Quarkus 3.40 LTS stream at
the time of this change.

Validation is performed with the backend `mvnw.cmd clean verify` command and
the existing PostgreSQL/pgvector integration test profile. No unrelated
dependency upgrades are included in this MVP issue.

The version is a delivery baseline, not a promise to remain unchanged. Before
official production delivery, repeat the supported-LTS review and update this
record with the current maintenance release and migration notes.

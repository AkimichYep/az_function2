# ThirdParty Integration Architecture

This diagram shows a production-oriented onboarding pattern for a third-party API using Java Azure Functions.

## High-Level Architecture (Pseudo Graphics)

```text
+----------------------------+        POST /api/thirdparty/sync         +------------------------------+
| Upstream System            | -----------------------------------------> | Azure Function App           |
| ERP / Pricing Source       |                                            | HTTP Trigger                 |
+----------------------------+                                            +------------------------------+
                                                                                     |
                                                                                     | Validate + Correlation ID
                                                                                     | + Idempotency Key
                                                                                     v
                                                                         +------------------------------+
                                                                         | Storage Queue / Service Bus  |
                                                                         +------------------------------+
                                                                                     |
                                                                                     v
                                                                         +------------------------------+
                                                                         | Azure Function App           |
                                                                         | Worker Trigger               |
                                                                         | (Queue/EventHub/ServiceBus) |
                                                                         +------------------------------+
                                                                           |           |             |
                                                                           |           |             +----------------------+
                                                                           |           |                                    |
                                                                           v           v                                    v
                                                         +------------------------+  +----------------------+     +----------------------+
                                                         | Mapping + Validation   |  | Idempotency Store    |     | Dead Letter Queue    |
                                                         +------------------------+  | Table/Cosmos/Postgres|     +----------------------+
                                                                           |          +----------------------+                
                                                                           v
                                                         +------------------------+
                                                         | ThirdParty API Client  |
                                                         | Retry/Backoff/Timeout  |
                                                         +------------------------+
                                                                           |
                                                                           v
                                                         +------------------------+
                                                         | ThirdParty API         |
                                                         +------------------------+

+----------------------+        telemetry/logs        +--------------------------+
| HTTP + Worker funcs  | ---------------------------> | Application Insights     |
+----------------------+                              +--------------------------+

+----------------------+      app settings refs       +--------------------------+
| Azure Key Vault      | ---------------------------> | HTTP + Worker funcs      |
+----------------------+                              +--------------------------+

+----------------------+       build/test/deploy      +--------------------------+
| GitHub Actions CI/CD | ---------------------------> | Function App (Dev/Test/Prod)
+----------------------+                              +--------------------------+
```

## Request/Processing Sequence (Pseudo Graphics)

```text
Actors:
  U = Upstream System
  H = HTTP Trigger
  Q = Queue/Topic
  W = Worker Trigger
  C = ThirdParty API
  D = Dead Letter Queue

Flow:
  U -> H : POST /api/thirdparty/sync
  H -> H : Validate payload + set correlationId
  H -> Q : Enqueue message
  H -> U : 202 Accepted

  Q -> W : Deliver message
  W -> W : Dedup/idempotency check
  W -> C : REST call (mapped payload)

  [if success]
    C -> W : 2xx
    W -> W : Mark processed + log success metrics

  [if transient failure: 429/5xx/timeout]
    C -> W : transient error
    W -> W : Retry with exponential backoff
    W -> C : Reattempt

  [if permanent failure]
    C -> W : permanent error
    W -> D : Move message with error context
```

## Core Design Decisions

- Use HTTP trigger as controlled ingress contract and fast integration boundary.
- Use asynchronous worker trigger to isolate third-party API latency/failures from callers.
- Enforce idempotency to handle at-least-once delivery safely.
- Use DLQ for non-recoverable failures with replay capability.
- Keep secrets in Key Vault references; never hardcode credentials.
- Use Application Insights for end-to-end traceability via correlation IDs.

## Suggested Azure Resources

- Function App (Java)
- Storage Account (queues) or Service Bus namespace
- Application Insights / Log Analytics
- Key Vault
- Optional idempotency store: Azure Table / Cosmos DB / PostgreSQL

## MVP Scope

- 1 HTTP endpoint (`/api/thirdparty/sync`)
- 1 async worker flow
- Retry/backoff + DLQ
- Correlation logging and basic dashboards
- CI/CD pipeline for dev deployment



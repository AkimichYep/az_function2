# Interview Notes - ThirdParty API Onboarding (Java + Azure Functions)

## Purpose
Use these notes to prepare for a Java Developer interview focused on onboarding a third-party API with Azure Functions, event-driven integration, and GitHub delivery.

---

## 1) 60-Second Intro Pitch
"I build reliable Java integrations on Azure with clear operational guardrails. For this third-party API onboarding, I would use an HTTP-triggered Azure Function as a controlled ingestion entry point, then process asynchronously through an event-driven layer for resiliency and scale. I focus on idempotency, retries, observability, and secure secret handling from day one. My execution style is incremental: prove a thin end-to-end flow quickly, then harden it with tests, monitoring, and CI/CD to support safe production rollout within the 2-month window."

---

## 2) Architecture You Can Explain on a Whiteboard
## High-level flow
1. Source system calls `POST /api/thirdparty/sync` (HTTP trigger).
2. Function validates payload, assigns correlation ID, and enqueues message.
3. Worker trigger (Queue/Event Hub) processes messages asynchronously.
4. Worker maps payload to target schema and calls third-party REST API.
5. Success/failure metrics and structured logs go to Application Insights.
6. Failures retry with backoff; poison messages route to DLQ for manual replay.

## Why this design
- HTTP entry gives controlled contract and quick integration path.
- Event-driven worker isolates external API volatility and rate limits.
- Async processing improves reliability and throughput.
- DLQ + idempotency make operations safe and auditable.

---

## 3) Estimation Talking Points (4 weeks effort in 2 months)
## Engineering effort (~4 weeks)
- Discovery + contract alignment: 3-4 days
- Skeleton + CI/CD baseline: 4-5 days
- Core integration build: 8-10 days
- Test hardening + UAT support: 5-6 days

## Why calendar window is 2 months
- External dependencies: API access, credentials, network approvals
- UAT scheduling with business users
- Production change windows and hypercare

## Confidence statement
"Estimate confidence is medium-high if API docs and sandbox access are available in week 1."

---

## 4) Technical Deep-Dive Answers
## Q: Why not call the third-party API directly in HTTP trigger only?
A: It works for simple cases, but coupling user/request latency to downstream API health is risky. I prefer an async worker for retries, buffering, and throughput smoothing.

## Q: How do you handle duplicate requests?
A: Generate or accept idempotency key per business entity and operation timestamp. Persist processing state (or dedup key) and make worker re-entrant.

## Q: How do you handle 429/5xx from external API?
A: Use bounded retries with exponential backoff and jitter, classify transient vs permanent errors, push exhausted failures to DLQ with correlation context.

## Q: What do you monitor?
A:
- Ingestion rate and processing latency
- Success/failure ratio by endpoint/status code
- Retry count and DLQ volume
- Dependency duration and timeout rates
- Correlation trace from ingress to outbound API call

## Q: How do you secure credentials?
A: Keep secrets in app settings via Key Vault references, avoid logging secrets, use least-privilege identities, define rotation procedure and test it.

---

## 5) REST Fundamentals to Mention
- Resource-oriented endpoint design and clear versioning
- Input validation and explicit error model
- Timeouts, retries, and connection reuse
- Pagination handling if third-party API responses are large
- Idempotency semantics for create/update operations

---

## 6) Event-Driven Integration Fundamentals to Mention
- At-least-once delivery implies duplicate-safe handlers
- Ordering assumptions should be explicit and tested
- Backpressure with queue/event hub + controlled concurrency
- DLQ playbook: inspect, fix, replay

---

## 7) Azure Functions Topics to Be Ready For
- Trigger types: HTTP, Queue, Event Hub, Timer
- App settings and environment separation (dev/test/prod)
- Host-level retries and function-level error handling
- Cold start implications and consumption plan tradeoffs
- Deployment slots or staged rollout strategy

---

## 8) GitHub + Delivery Talking Points
- PR-first workflow with small, reviewable changes
- Branch protection + required checks
- GitHub Actions pipeline: build, test, package, deploy
- Environment-specific configuration and approvals
- Rollback strategy and release tagging

---

## 9) STAR Stories You Can Reuse
## Story A - Stabilized flaky external API integration
- Situation: External partner API had intermittent 429/500 errors.
- Task: Keep data flow reliable without operator overload.
- Action: Added async queue worker, retry/backoff, idempotency keys, and DLQ.
- Result: Improved success rate and reduced manual incident handling.

## Story B - Improved deployment safety
- Situation: Frequent hotfixes created release risk.
- Task: Make deployments predictable.
- Action: Implemented CI checks, gated releases, and runbook-driven rollout.
- Result: Fewer release regressions and faster recovery when issues appeared.

Tip: Replace results with your real numbers when available.

---

## 10) Questions to Ask Interviewers
1. Which third-party endpoints are in MVP scope first?
2. What auth model is used (API key, OAuth, mTLS)?
3. Expected volume, SLA, and retry tolerance?
4. Is processing synchronous or event-driven from day one?
5. How is production observability handled today?
6. What does success look like after 30/60 days?

---

## 11) 30-60-90 Day Plan (if asked)
## First 30 days
- Finalize contract and auth setup, ship MVP flow to sandbox.

## 60 days
- Production-ready hardening: retries, idempotency, dashboards, runbook.

## 90 days
- Expand endpoint coverage, optimize cost/performance, reduce manual operations.

---

## 12) Quick Checklist Before Interview
- Review `THIRDPARTY_API_ONBOARDING_PLAN.md`
- Be ready to draw architecture in 2-3 minutes
- Prepare one real example for retries/idempotency
- Prepare one CI/CD example from GitHub Actions
- Prepare one production incident story and what you learned

---

## 13) One-Liner Close
"I can deliver the first working third-party integration quickly, then harden it into a production-safe pipeline with clear observability and controlled release practices."



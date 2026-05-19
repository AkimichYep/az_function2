# Mock ClearDemand Lab (H2 -> Public API)

This lab simulates ClearDemand onboarding by using:
- an on-prem-style H2 database (`products`, `prices`)
- an Azure HTTP-triggered Java Function
- a public API endpoint as a stand-in for ClearDemand (`https://httpbin.org/post`)

## Function
- Name: `ClearDemandMockSyncJava`
- Route: `POST /api/cleardemand/sync-mock`
- Optional query/body param: `maxRecords` (default `5`, max `100`)

## Runtime settings
Configured in `local.settings.json`:
- `OnPremH2JdbcUrl`
- `OnPremH2User`
- `OnPremH2Password`
- `MockClearDemandApiUrl`

## Local run
```powershell
Set-Location "C:\Users\Maksym_Yepaneshnikov\azure-function-examples"
mvn test
mvn azure-functions:run
```

## Trigger a sync
```powershell
Invoke-RestMethod -Method POST "http://localhost:7071/api/cleardemand/sync-mock?maxRecords=3"
```

Expected response contains summary like:
- `total=3`
- `success=3`
- `failed=0`

## What happens during execution
1. Function creates H2 tables if missing.
2. Inserts sample rows if tables are empty.
3. Reads latest `products` + `prices` rows.
4. Sends one POST per row to `MockClearDemandApiUrl`.
5. Waits for each response and returns aggregated result.

## Azure deployment notes
When deployed, set these app settings on Function App:
- `OnPremH2JdbcUrl` (for persistence, prefer file-based H2 URL if needed)
- `OnPremH2User`
- `OnPremH2Password`
- `MockClearDemandApiUrl`

Example file-based H2 URL for longer-lived data:
- `jdbc:h2:file:/home/site/wwwroot/data/onpremdb;MODE=PostgreSQL;AUTO_SERVER=TRUE`

Keep your existing required Azure settings (for host startup):
- `AzureWebJobsStorage`
- `FUNCTIONS_WORKER_RUNTIME=java`


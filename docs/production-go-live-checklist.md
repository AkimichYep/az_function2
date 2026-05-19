# Production Go-Live Checklist (Azure Functions)

This checklist is tailored for:
- Function App: `azure-function-examples-1779091049777`
- Resource Group: `attempt2-rg`

## 1) Freeze and version
- Create a release tag for the commit you want to ship.
- Build once from clean state.

```powershell
Set-Location "C:\Users\Maksym_Yepaneshnikov\azure-function-examples"
git status
mvn clean test
mvn -DskipTests package
```

## 2) Lock down HTTP surface
Current HTTP functions are anonymous by default in source, which is not recommended for production.

Recommended:
- Change `AuthorizationLevel.ANONYMOUS` to `AuthorizationLevel.FUNCTION` for production endpoints.
- Keep only required routes enabled.

After deploying, test with function key.

## 3) Secrets and configuration hygiene
- Move secrets from app settings to Key Vault references.
- Rotate any exposed keys/passwords before go-live.
- Keep non-production integrations disabled in prod if not used.

Suggested app settings to review:
- `AzureWebJobsStorage`
- `EventHubConnection`
- `PostgreSQLConnection` (if used)
- `AzureWebJobs.CosmosDBTriggerJava.Disabled`
- `AzureWebJobs.EventHubTriggerJava.Disabled`
- `AzureWebJobs.ServiceBusTriggerJava.Disabled`

## 4) Observability and alerting
- Ensure Application Insights is connected.
- Add alerts for:
  - Function failures > 0
  - 5xx responses
  - High average duration
  - Exception count spike

## 5) Identity and access
- Prefer Managed Identity where supported.
- Grant least-privilege roles only.
- Restrict deployment permissions to release identities.

## 6) Network and exposure
Current state shows `publicNetworkAccess=Enabled`.

Production options:
- Keep public access, but protect with auth/API gateway.
- Or private endpoints/VNet integration for internal-only access.

## 7) Deploy and smoke test

```powershell
az account set --subscription "90cc9c81-c136-42ee-bb35-6a096182dc9d"
mvn -DskipTests package
$stage = "C:\Users\Maksym_Yepaneshnikov\azure-function-examples\target\azure-functions\azure-function-examples-1779091049777"
$zip = "C:\Users\Maksym_Yepaneshnikov\azure-function-examples\target\azure-function-examples-1779091049777-release.zip"
if (Test-Path $zip) { Remove-Item $zip -Force }
Compress-Archive -Path "$stage\*" -DestinationPath $zip

az functionapp deployment source config-zip `
  --resource-group "attempt2-rg" `
  --name "azure-function-examples-1779091049777" `
  --src $zip
```

Why this path: it deploys to your existing app directly and avoids Maven plugin trying to create/modify App Service plan resources.

Smoke test (replace key):

```powershell
$functionKey = "<FUNCTION_KEY>"
Invoke-WebRequest -Method POST `
  -Uri ("https://azure-function-examples-1779091049777.azurewebsites.net/api/thirdparty/sync-mock?maxRecords=1&code=" + $functionKey) `
  -UseBasicParsing | Select-Object -ExpandProperty Content
```

## 8) Rollback readiness
- Keep previous known-good package artifact.
- Document rollback command and owner.
- Validate rollback in non-prod at least once.

## 9) Post go-live (first 24-48h)
- Monitor logs and failures in near-real-time.
- Check throughput, timeout rate, and external API errors.
- Tune retry/backoff only if data supports it.

## Notes from current environment checks
- Function App is running and HTTPS-only.
- `publicNetworkAccess` is enabled.
- `alwaysOn` is false (normal on Consumption plan).



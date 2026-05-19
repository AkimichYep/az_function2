# Getting AzureWebJobsStorage Connection String

## 📍 Where to Find It

### Option 1: Using Azure Portal (Easiest)

1. **Go to Azure Portal** → https://portal.azure.com
2. **Search for Storage Accounts** in the search bar
3. **Find or create** a storage account in your resource group (`attempt2-rg`)
4. **Click on the storage account**
5. **Left sidebar → Settings → Access keys**
6. **Copy the "Connection string"** under Key 1 or Key 2

**Example what it looks like:**
```
DefaultEndpointsProtocol=https;AccountName=mystorageacct;AccountKey=AbCdEfGhIJKLmnOPqrsTUVwxyz123456789==;EndpointSuffix=core.windows.net
```

---

### Option 2: Create a New Storage Account (If You Don't Have One)

1. **Azure Portal → Create a resource**
2. **Search: Storage account**
3. **Create**
4. **Subscription:** Same as your Cosmos DB
5. **Resource group:** `attempt2-rg`
6. **Storage account name:** Something like `storageaksdev` (must be lowercase, no special chars)
7. **Region:** West Europe (same as your Cosmos DB)
8. **Review + Create**
9. Once created, go to **Access keys** and copy the connection string

---

### Option 3: Using Azure CLI

```bash
# List storage accounts
az storage account list --resource-group attempt2-rg

# Get connection string for specific account
az storage account show-connection-string \
  --name <storage-account-name> \
  --resource-group attempt2-rg
```

---

## 🔍 Understanding Your Connection String

```
DefaultEndpointsProtocol=https;AccountName=mystorageacct;AccountKey=XXXXX==;EndpointSuffix=core.windows.net
                                                           ↑
                                              Your storage account name
```

| Part | Meaning |
|------|---------|
| `DefaultEndpointsProtocol=https` | Use secure HTTPS |
| `AccountName=mystorageacct` | Your storage account name |
| `AccountKey=XXXXX==` | Secret key (keep confidential!) |
| `EndpointSuffix=core.windows.net` | Azure public cloud |

---

## ✅ Update local.settings.json

Once you have the connection string:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "DefaultEndpointsProtocol=https;AccountName=mystorageacct;AccountKey=YOUR_ACTUAL_KEY_HERE==;EndpointSuffix=core.windows.net",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "PostgreSQLConnection": "jdbc:postgresql://c-az-cosmos-db.2pu3zyii5mixai.postgres.cosmos.azure.com:5432/citus?user=citus&password=AzureDbRoot43<>&sslmode=require"
  }
}
```

---

## 📌 What Does AzureWebJobsStorage Do?

This connection string is used by Azure Functions for:
- **Queue storage** (if you use Queue triggers)
- **Blob storage** (if you use Blob triggers)
- **Internal state management** (local execution)
- **Logging** (when deployed to Azure)

For local testing of your PostgreSQL trigger: **It's optional** (can leave placeholder)

For local testing of Queue/Blob triggers: **Required**

For Azure deployment: **Required**

---

## 🚀 Quick Action Items

1. **Go to Azure Portal**
2. **Find your storage account** (or create one)
3. **Copy Connection string** from Access keys
4. **Update local.settings.json** with the full string
5. **Save the file**
6. **Run**: `mvn azure-functions:run`

---

## ⚠️ Security Note

- **Never commit** `local.settings.json` to Git (already in .gitignore ✅)
- **Keep your storage key confidential**
- **In production**, use Azure Key Vault for secrets

---

## 📝 Example Storage Account Creation (Azure CLI)

```bash
# Create storage account
az storage account create \
  --name storageaksdev \
  --resource-group attempt2-rg \
  --location westeurope \
  --sku Standard_LRS

# Get connection string
az storage account show-connection-string \
  --name storageaksdev \
  --resource-group attempt2-rg \
  --query connectionString \
  --output tsv
```

---

**Next Step:** Get your storage account connection string and update `local.settings.json` 🚀


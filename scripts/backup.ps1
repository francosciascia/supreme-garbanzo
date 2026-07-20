$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$backupDirectory = Join-Path $projectRoot "backups"
New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupFile = Join-Path $backupDirectory "franco-$timestamp.sql"
docker compose -f (Join-Path $projectRoot "compose.yaml") exec -T db pg_dump -U franco -d franco --clean --if-exists --no-owner | Set-Content -Path $backupFile -Encoding utf8
Write-Host "Backup creado: $backupFile"

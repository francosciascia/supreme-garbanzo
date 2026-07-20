param([Parameter(Mandatory = $true)][string]$BackupFile)
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$resolvedBackup = Resolve-Path -LiteralPath $BackupFile -ErrorAction Stop
if (-not $resolvedBackup.Path.StartsWith($projectRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "El backup debe estar dentro del proyecto."
}
$confirmation = Read-Host "Esto reemplazará los datos actuales. Escribí RESTAURAR para continuar"
if ($confirmation -ne "RESTAURAR") { Write-Host "Restauración cancelada"; exit 1 }
Get-Content -LiteralPath $resolvedBackup.Path -Raw | docker compose -f (Join-Path $projectRoot "compose.yaml") exec -T db psql -U franco -d franco
Write-Host "Base restaurada desde: $($resolvedBackup.Path)"

param(
    [Parameter(Mandatory = $true)]
    [string]$ScreenPath
)

$resolvedPath = Resolve-Path -LiteralPath $ScreenPath -ErrorAction Stop
$kotlinFiles = @(Get-ChildItem -LiteralPath $resolvedPath -Recurse -File -Filter '*.kt')
$failures = [System.Collections.Generic.List[string]]::new()

foreach ($file in $kotlinFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw
    $platformWrapper = $content -match '(?m)^\s*(?:internal\s+)?(?:expect|actual)\b'

    if ($file.Name -match 'Preview\.kt$') {
        $failures.Add("独立 Preview 文件不允许：$($file.FullName)")
    }

    if (-not $platformWrapper -and $content -match '@Composable' -and $content -notmatch '@AppPreview') {
        $failures.Add("含有 @Composable 但没有同文件 @AppPreview：$($file.FullName)")
    }

    if ($file.Name -match 'Ext\.kt$') {
        if ($content -match '@Composable|@Preview') {
            $failures.Add("Ext.kt 不得包含 Compose 或 Preview：$($file.FullName)")
        }
        if ($content -match '(?m)^\s*private\s+(?:fun|val|var)\s+\w+\.') {
            $failures.Add("Ext.kt 不得包含 private 扩展：$($file.FullName)")
        }
    }
}

$templateRoot = Join-Path (Split-Path -Parent $PSScriptRoot) 'templates'
$templateFiles = @(Get-ChildItem -LiteralPath $templateRoot -File -Filter '*.template')
foreach ($file in $templateFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw
    if ($content -match 'androidx\.compose\.material\.icons|com\.ciyin\.app\.ui\.util\.AppPreview') {
        $failures.Add("模板包含已禁止的 Material Icons 或旧 Preview import：$($file.FullName)")
    }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output "create-kmp-screen 静态检查通过：$($kotlinFiles.Count) 个 Kotlin 文件"

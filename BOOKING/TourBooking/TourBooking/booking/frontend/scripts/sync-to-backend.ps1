param(
  [string]$FrontendPath = "$(Resolve-Path (Join-Path $PSScriptRoot '..'))",
  [string]$BackendStaticPath = "$(Resolve-Path (Join-Path $PSScriptRoot '..\\..\\booking\\src\\main\\resources\\static'))"
)

Write-Host "Sync frontend -> backend static"
Write-Host "Frontend: $FrontendPath"
Write-Host "Backend static: $BackendStaticPath"

$pages = Join-Path $FrontendPath "pages"
$assets = Join-Path $FrontendPath "assets"

if (!(Test-Path $pages)) { throw "Missing pages folder: $pages" }
if (!(Test-Path $assets)) { throw "Missing assets folder: $assets" }

if (!(Test-Path $BackendStaticPath)) { New-Item -ItemType Directory -Force -Path $BackendStaticPath | Out-Null }

# Clean old static (only known folders)
@("assets","pages") | ForEach-Object {
  $p = Join-Path $BackendStaticPath $_
  if (Test-Path $p) { Remove-Item -Recurse -Force $p }
}

Copy-Item -Recurse -Force $assets (Join-Path $BackendStaticPath "assets")
Copy-Item -Recurse -Force $pages (Join-Path $BackendStaticPath "pages")

Write-Host "Done."


# ==========================================
# Clean Git Push Script (PowerShell)
# ==========================================

Write-Host "🧹 [Git] Cleaning up git state..." -ForegroundColor Yellow
if (Test-Path ".git/index") {
    Remove-Item .git/index -Force
}
git reset

Write-Host "🚀 Staging current project files..." -ForegroundColor Green
git add -A

Write-Host "📦 Committing changes..." -ForegroundColor Green
git commit -m "feat: Update project source code" --allow-empty

Write-Host "📤 Pushing to GitHub..." -ForegroundColor Green
git push origin main

Write-Host "✨ Done!" -ForegroundColor Cyan

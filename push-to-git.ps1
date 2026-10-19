# ==========================================
# GitHub Repository Push Automation Script (PowerShell)
# Repository: https://github.com/jwlee6312-crypto/smart-erp-crm
# ==========================================

Write-Host "🚀 [Git] Staging project files..." -ForegroundColor Green
git add .

Write-Host "📦 [Git] Committing changes..." -ForegroundColor Green
git commit -m "chore: Clean up project structure and finalize clean repository"

Write-Host "🔗 [Git] Connecting remote repository..." -ForegroundColor Green
git remote remove origin 2>$null
git remote add origin https://github.com/jwlee6312-crypto/smart-erp-crm.git

Write-Host "🌿 [Git] Setting branch to main..." -ForegroundColor Green
git branch -M main

Write-Host "📤 [Git] Pushing code to GitHub (https://github.com/jwlee6312-crypto/smart-erp-crm)..." -ForegroundColor Green
git push -u origin main

Write-Host "✨ [Git] Successfully pushed project to GitHub!" -ForegroundColor Cyan

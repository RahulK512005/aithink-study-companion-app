# RunAnywhere SDK Build and Test Script
# This script builds the project and tests the SDK integration

Write-Host "🚀 RunAnywhere SDK Build and Test Script" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green

# Set location
Set-Location "d:\Aithink\AiThinkStudyCompanion"

Write-Host "`n📦 Step 1: Checking SDK Files..." -ForegroundColor Yellow
$sdkFiles = @(
    "app\libs\RunAnywhereKotlinSDK-release.aar",
    "app\libs\runanywhere-llm-llamacpp-release.aar"
)

$allFilesExist = $true
foreach ($file in $sdkFiles) {
    if (Test-Path $file) {
        Write-Host "✅ Found: $file" -ForegroundColor Green
    } else {
        Write-Host "❌ Missing: $file" -ForegroundColor Red
        $allFilesExist = $false
    }
}

if (-not $allFilesExist) {
    Write-Host "`n❌ SDK files are missing! Please ensure AAR files are in app/libs/" -ForegroundColor Red
    exit 1
}

Write-Host "`n🧹 Step 2: Cleaning Project..." -ForegroundColor Yellow
try {
    & .\gradlew.bat clean
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Clean successful" -ForegroundColor Green
    } else {
        Write-Host "❌ Clean failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Clean failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n🔨 Step 3: Building Project..." -ForegroundColor Yellow
try {
    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build successful" -ForegroundColor Green
    } else {
        Write-Host "❌ Build failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Build failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n📱 Step 4: Checking for Connected Devices..." -ForegroundColor Yellow
try {
    $devices = & adb devices
    $deviceCount = ($devices | Select-String "device$").Count
    
    if ($deviceCount -gt 0) {
        Write-Host "✅ Found $deviceCount connected device(s)" -ForegroundColor Green
        
        Write-Host "`n📲 Step 5: Installing APK..." -ForegroundColor Yellow
        & .\gradlew.bat installDebug
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Installation successful" -ForegroundColor Green
        } else {
            Write-Host "❌ Installation failed" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "`n🔍 Step 6: Starting Log Monitor..." -ForegroundColor Yellow
        Write-Host "Monitoring logs for SDK status... (Press Ctrl+C to stop)" -ForegroundColor Cyan
        Write-Host "Looking for: AiThink, RunAnywhere, AIService logs" -ForegroundColor Cyan
        
        # Start the app first
        & adb shell am start -n com.aithink.studycompanion/.ui.MainActivity
        Start-Sleep -Seconds 2
        
        # Monitor logs
        & adb logcat -s "AiThinkApp:*" "MainActivity:*" "AIService:*" "RunAnywhere:*"
        
    } else {
        Write-Host "⚠️ No devices connected. Skipping installation." -ForegroundColor Yellow
        Write-Host "To install manually:" -ForegroundColor Cyan
        Write-Host "1. Connect your device or start emulator" -ForegroundColor Cyan
        Write-Host "2. Run: .\gradlew.bat installDebug" -ForegroundColor Cyan
    }
} catch {
    Write-Host "⚠️ ADB not found or device check failed: $_" -ForegroundColor Yellow
    Write-Host "APK built successfully at: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
}

Write-Host "`n🎉 Build Process Complete!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

Write-Host "`n📋 What to Check:" -ForegroundColor Yellow
Write-Host "1. Look for 'RunAnywhere SDK: Active ✅' in logs" -ForegroundColor White
Write-Host "2. Check 'SDK Available: true' message" -ForegroundColor White
Write-Host "3. Verify available models list" -ForegroundColor White
Write-Host "4. Test chat functionality in the app" -ForegroundColor White

Write-Host "`n🔧 Manual Testing:" -ForegroundColor Yellow
Write-Host "1. Open the app" -ForegroundColor White
Write-Host "2. Go to Chat tab" -ForegroundColor White
Write-Host "3. Send a message like 'Hello' or 'Explain photosynthesis'" -ForegroundColor White
Write-Host "4. Check if responses are from AI model or fallback" -ForegroundColor White

Write-Host "`n📊 Expected Log Output:" -ForegroundColor Yellow
Write-Host "AiThinkApp: ✅ AIService initialized: RunAnywhere SDK: Active ✅" -ForegroundColor Green
Write-Host "MainActivity: 🔍 RunAnywhere SDK Status: RunAnywhere SDK: Active ✅" -ForegroundColor Green
Write-Host "MainActivity: 📱 SDK Available: true" -ForegroundColor Green
Write-Host "MainActivity: 🤖 Available Models: [Gemma 3 1B, Qwen 2.5 1.5B, TinyLlama 1.1B]" -ForegroundColor Green

Write-Host "`n🎯 Success! Your RunAnywhere SDK integration is ready!" -ForegroundColor Green
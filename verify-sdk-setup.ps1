# RunAnywhere SDK Setup Verification Script
Write-Host "🔍 RunAnywhere SDK Setup Verification" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

Set-Location "d:\Aithink\AiThinkStudyCompanion"

Write-Host "`n📦 Checking SDK Files..." -ForegroundColor Yellow

# Check AAR files
$aarFiles = @(
    @{Path="app\libs\RunAnywhereKotlinSDK-release.aar"; Name="RunAnywhere Kotlin SDK"},
    @{Path="app\libs\runanywhere-llm-llamacpp-release.aar"; Name="RunAnywhere LLM LlamaCpp"}
)

$aarCount = 0
foreach ($aar in $aarFiles) {
    if (Test-Path $aar.Path) {
        $size = (Get-Item $aar.Path).Length / 1MB
        Write-Host "✅ $($aar.Name): $($aar.Path) ($([math]::Round($size, 2)) MB)" -ForegroundColor Green
        $aarCount++
    } else {
        Write-Host "❌ Missing: $($aar.Name) at $($aar.Path)" -ForegroundColor Red
    }
}

Write-Host "`n🔧 Checking Build Configuration..." -ForegroundColor Yellow

# Check build.gradle.kts
$buildGradle = "app\build.gradle.kts"
if (Test-Path $buildGradle) {
    $content = Get-Content $buildGradle -Raw
    
    $checks = @(
        @{Pattern='implementation\(files\("libs/RunAnywhereKotlinSDK-release\.aar"\)\)'; Name="Kotlin SDK dependency"},
        @{Pattern='implementation\(files\("libs/runanywhere-llm-llamacpp-release\.aar"\)\)'; Name="LLM SDK dependency"},
        @{Pattern='buildConfigField\("String", "RUNANYWHERE_API_KEY"'; Name="API key configuration"},
        @{Pattern='abiFilters \+= listOf\("arm64-v8a", "armeabi-v7a"\)'; Name="ABI filters"},
        @{Pattern='useLegacyPackaging = true'; Name="Legacy packaging"}
    )
    
    foreach ($check in $checks) {
        if ($content -match $check.Pattern) {
            Write-Host "✅ $($check.Name)" -ForegroundColor Green
        } else {
            Write-Host "❌ Missing: $($check.Name)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ build.gradle.kts not found" -ForegroundColor Red
}

Write-Host "`n🏗️ Checking Application Setup..." -ForegroundColor Yellow

# Check AiThinkApplication.kt
$appFile = "app\src\main\kotlin\com\aithink\studycompanion\AiThinkApplication.kt"
if (Test-Path $appFile) {
    $content = Get-Content $appFile -Raw
    
    $appChecks = @(
        @{Pattern='import com\.aithink\.studycompanion\.data\.service\.AIService'; Name="AIService import"},
        @{Pattern='lateinit var aiService: AIService'; Name="AIService property"},
        @{Pattern='aiService = AIService\.createWithContext\(this\)'; Name="AIService initialization"},
        @{Pattern='fun getAIService\(\): AIService'; Name="AIService getter"}
    )
    
    foreach ($check in $appChecks) {
        if ($content -match $check.Pattern) {
            Write-Host "✅ $($check.Name)" -ForegroundColor Green
        } else {
            Write-Host "❌ Missing: $($check.Name)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ AiThinkApplication.kt not found" -ForegroundColor Red
}

Write-Host "`n🤖 Checking AIService Implementation..." -ForegroundColor Yellow

# Check AIService.kt
$serviceFile = "app\src\main\kotlin\com\aithink\studycompanion\data\service\AIService.kt"
if (Test-Path $serviceFile) {
    $content = Get-Content $serviceFile -Raw
    
    $serviceChecks = @(
        @{Pattern='class AIService\(private val context: Context\? = null\)'; Name="Context-aware constructor"},
        @{Pattern='private fun initializeSDK\(\)'; Name="SDK initialization method"},
        @{Pattern='Class\.forName\("ai\.runanywhere\.llm\.LLMInference"\)'; Name="SDK class loading"},
        @{Pattern='fun isSDKAvailable\(\): Boolean'; Name="SDK availability check"},
        @{Pattern='fun getSDKStatus\(\): String'; Name="SDK status method"},
        @{Pattern='companion object'; Name="Companion object for factory method"}
    )
    
    foreach ($check in $serviceChecks) {
        if ($content -match $check.Pattern) {
            Write-Host "✅ $($check.Name)" -ForegroundColor Green
        } else {
            Write-Host "❌ Missing: $($check.Name)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ AIService.kt not found" -ForegroundColor Red
}

Write-Host "`n📱 Checking MainActivity..." -ForegroundColor Yellow

# Check MainActivity.kt
$mainFile = "app\src\main\kotlin\com\aithink\studycompanion\ui\MainActivity.kt"
if (Test-Path $mainFile) {
    $content = Get-Content $mainFile -Raw
    
    if ($content -match 'aiService\.getSDKStatus\(\)') {
        Write-Host "✅ SDK status logging" -ForegroundColor Green
    } else {
        Write-Host "❌ Missing: SDK status logging" -ForegroundColor Red
    }
} else {
    Write-Host "❌ MainActivity.kt not found" -ForegroundColor Red
}

Write-Host "`n📋 Summary:" -ForegroundColor Yellow
Write-Host "AAR Files: $aarCount/2 found" -ForegroundColor $(if ($aarCount -eq 2) { "Green" } else { "Red" })

if ($aarCount -eq 2) {
    Write-Host "`n🎉 Setup looks good! Ready to build and test." -ForegroundColor Green
    Write-Host "`nNext steps:" -ForegroundColor Cyan
    Write-Host "1. Run: .\build-and-test-sdk.ps1" -ForegroundColor White
    Write-Host "2. Check logs for SDK status" -ForegroundColor White
    Write-Host "3. Test app functionality" -ForegroundColor White
} else {
    Write-Host "`n⚠️ Setup incomplete. Please check missing items above." -ForegroundColor Yellow
    Write-Host "`nTo fix:" -ForegroundColor Cyan
    Write-Host "1. Ensure AAR files are in app/libs/" -ForegroundColor White
    Write-Host "2. Check build configuration" -ForegroundColor White
    Write-Host "3. Verify code changes" -ForegroundColor White
}

Write-Host "`n📖 For detailed setup info, see: RUNANYWHERE_SDK_SETUP_COMPLETE.md" -ForegroundColor Cyan
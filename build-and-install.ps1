$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AiThink Study Companion - Build & Install" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Download Gradle wrapper JAR if not exists
$wrapperDir = "gradle\wrapper"
$wrapperJar = "$wrapperDir\gradle-wrapper.jar"

if (!(Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Force -Path $wrapperDir | Out-Null
}

if (!(Test-Path $wrapperJar)) {
    Write-Host "[1/4] Downloading Gradle wrapper..." -ForegroundColor Yellow
    $wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.1.1/gradle/wrapper/gradle-wrapper.jar"
    try {
        Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar -UseBasicParsing
        Write-Host "[OK] Gradle wrapper downloaded" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Failed to download Gradle wrapper" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[1/4] Gradle wrapper already exists" -ForegroundColor Green
}

# Step 2: Build the APK
Write-Host ""
Write-Host "[2/4] Building APK (this may take 2-5 minutes)..." -ForegroundColor Yellow
Write-Host "Please wait..." -ForegroundColor Gray

try {
    & .\gradlew.bat assembleDebug --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed with exit code $LASTEXITCODE"
    }
    Write-Host "[OK] APK built successfully" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Build failed" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "TIP: Try opening the project in Android Studio instead:" -ForegroundColor Yellow
    Write-Host "  1. Open Android Studio" -ForegroundColor Gray
    Write-Host "  2. File -> Open -> Select this folder" -ForegroundColor Gray
    Write-Host "  3. Wait for Gradle sync" -ForegroundColor Gray
    Write-Host "  4. Click Run button" -ForegroundColor Gray
    exit 1
}

# Step 3: Find the APK
Write-Host ""
Write-Host "[3/4] Locating APK..." -ForegroundColor Yellow

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (!(Test-Path $apkPath)) {
    Write-Host "[ERROR] APK not found at: $apkPath" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
$apkSizeStr = [math]::Round($apkSize, 2)
Write-Host "[OK] APK found: $apkPath ($apkSizeStr MB)" -ForegroundColor Green

# Step 4: Install to emulator
Write-Host ""
Write-Host "[4/4] Installing to emulator..." -ForegroundColor Yellow

$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (!(Test-Path $adbPath)) {
    Write-Host "[ERROR] ADB not found at: $adbPath" -ForegroundColor Red
    Write-Host "Please ensure Android SDK is installed" -ForegroundColor Red
    exit 1
}

# Check for connected devices
$devices = & $adbPath devices
Write-Host "Connected devices:" -ForegroundColor Gray
Write-Host $devices -ForegroundColor Gray

if ($devices -match "emulator-\d+\s+device") {
    try {
        & $adbPath install -r $apkPath
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "========================================" -ForegroundColor Green
            Write-Host "SUCCESS! APP INSTALLED" -ForegroundColor Green
            Write-Host "========================================" -ForegroundColor Green
            Write-Host ""
            Write-Host "The app is now installed on your emulator!" -ForegroundColor Cyan
            Write-Host "Look for 'AiThink Study Companion' in the app drawer" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "To launch the app:" -ForegroundColor Yellow
            Write-Host "  & '$adbPath' shell am start -n com.aithink.studycompanion/.ui.MainActivity" -ForegroundColor Gray
        } else {
            throw "Installation failed with exit code $LASTEXITCODE"
        }
    } catch {
        Write-Host "[ERROR] Installation failed" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[ERROR] No emulator detected" -ForegroundColor Red
    Write-Host "Please start an Android emulator first" -ForegroundColor Yellow
    exit 1
}

# 📱 Install APK on Connected Emulator - Step by Step

## ✅ Prerequisites

You need one of the following:

1. **Android Studio** with an emulator running, OR
2. **Android SDK** installed with `adb` in your PATH

---

## 🚀 Method 1: Using Android Studio (EASIEST - Recommended)

### Step 1: Open Project in Android Studio

1. Launch **Android Studio**
2. Click **File → Open**
3. Navigate to: `D:\Aithink\AiThinkStudyCompanion`
4. Click **OK**
5. Wait for Gradle sync (2-3 minutes)

### Step 2: Start Emulator

1. Click **Tools → Device Manager** (or click the phone icon in toolbar)
2. Find your emulator in the list
3. Click the **Play button** (▶️) to start it
4. Wait 30-60 seconds for emulator to boot

### Step 3: Run App (Builds & Installs Automatically)

1. Click the **Run** button (▶️ green play icon) in toolbar, OR
2. Press **Shift+F10**, OR
3. Menu: **Run → Run 'app'**

4. Select your emulator from the device list
5. Click **OK**

**That's it!** Android Studio will:

- Build the APK
- Install it on the emulator
- Launch the app automatically

---

## 🚀 Method 2: Build Then Drag & Drop

### Step 1: Build APK in Android Studio

1. Open project in Android Studio
2. Wait for Gradle sync
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. Wait for build to complete (2-5 minutes)
5. Click **locate** in the notification

### Step 2: Drag & Drop to Emulator

1. Make sure your emulator is running
2. Locate the APK:
   ```
   D:\Aithink\AiThinkStudyCompanion\app\build\outputs\apk\debug\app-debug.apk
   ```
3. **Drag and drop** the `app-debug.apk` file onto the emulator window
4. APK will automatically install
5. Open the app from the app drawer

---

## 🚀 Method 3: Using ADB Command Line

### Step 1: Find ADB

ADB is typically located at:

```
C:\Users\YourUsername\AppData\Local\Android\Sdk\platform-tools\adb.exe
```

Or if you have Android Studio:

```
C:\Program Files\Android\Android Studio\jbr\bin\adb.exe
```

### Step 2: Add ADB to PATH (Optional)

**Windows (PowerShell as Administrator):**

```powershell
$path = [Environment]::GetEnvironmentVariable('Path', 'User')
$androidSdk = "$env:LOCALAPPDATA\Android\Sdk\platform-tools"
[Environment]::SetEnvironmentVariable('Path', "$path;$androidSdk", 'User')
```

Then restart PowerShell.

### Step 3: Check Connected Devices

```powershell
adb devices
```

You should see output like:

```
List of devices attached
emulator-5554   device
```

If you see "unauthorized", accept the prompt on the emulator.

### Step 4: Build APK

**Option A: Using Android Studio**

- Open project → Build → Build APK(s)

**Option B: Using Gradle Wrapper**

```powershell
cd D:\Aithink\AiThinkStudyCompanion
.\gradlew.bat assembleDebug
```

### Step 5: Install APK

```powershell
cd D:\Aithink\AiThinkStudyCompanion
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The `-r` flag reinstalls the app if it's already installed.

### Step 6: Launch App

```powershell
adb shell am start -n com.aithink.studycompanion/.ui.MainActivity
```

---

## 🚀 Method 4: One-Click PowerShell Script

### Create Installation Script

Create a file named `install-to-emulator.ps1` in the project root:

```powershell
# AiThink Study Companion - Install to Emulator
Write-Host "🚀 Building and Installing AiThink Study Companion..." -ForegroundColor Cyan

# Navigate to project directory
Set-Location $PSScriptRoot

# Find ADB
$adbPaths = @(
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    "C:\Android\Sdk\platform-tools\adb.exe",
    "C:\Program Files\Android\Android Studio\jbr\bin\adb.exe"
)

$adb = $null
foreach ($path in $adbPaths) {
    if (Test-Path $path) {
        $adb = $path
        Write-Host "✅ Found ADB at: $adb" -ForegroundColor Green
        break
    }
}

if (-not $adb) {
    Write-Host "❌ ADB not found. Please install Android SDK or Android Studio." -ForegroundColor Red
    Write-Host "Or specify ADB path manually." -ForegroundColor Yellow
    exit 1
}

# Check for connected devices
Write-Host "`n📱 Checking for connected devices..." -ForegroundColor Cyan
& $adb devices

$devices = & $adb devices | Select-String "device$" | Measure-Object
if ($devices.Count -eq 0) {
    Write-Host "❌ No devices connected. Please start an emulator." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Device found!" -ForegroundColor Green

# Check if gradlew exists
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "⚠️  Gradle wrapper not found." -ForegroundColor Yellow
    Write-Host "Please open project in Android Studio first to generate wrapper files." -ForegroundColor Yellow
    Write-Host "Or build APK in Android Studio: Build → Build Bundle(s) / APK(s) → Build APK(s)" -ForegroundColor Yellow
    exit 1
}

# Build APK
Write-Host "`n🔨 Building APK..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful!" -ForegroundColor Green

# Install APK
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "`n📲 Installing APK..." -ForegroundColor Cyan
& $adb install -r $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Installation successful!" -ForegroundColor Green
    
    # Launch app
    Write-Host "`n🚀 Launching app..." -ForegroundColor Cyan
    & $adb shell am start -n com.aithink.studycompanion/.ui.MainActivity
    
    Write-Host "`n🎉 Done! AiThink Study Companion is now running on your emulator." -ForegroundColor Green
} else {
    Write-Host "❌ Installation failed!" -ForegroundColor Red
    exit 1
}
```

### Run the Script

```powershell
cd D:\Aithink\AiThinkStudyCompanion
.\install-to-emulator.ps1
```

---

## 🐛 Troubleshooting

### Issue: "No devices connected"

**Solution:**

1. Start your emulator:
    - Open Android Studio
    - Tools → Device Manager
    - Click Play button on your emulator
2. Wait 30-60 seconds for emulator to fully boot
3. Try again

### Issue: "ADB not found"

**Solution:**

1. Install Android Studio (includes SDK and ADB)
2. Or download SDK Platform Tools:
    - https://developer.android.com/studio/releases/platform-tools
3. Add to PATH or use full path to adb.exe

### Issue: "Unauthorized device"

**Solution:**

1. Look at emulator screen
2. You'll see a popup: "Allow USB debugging?"
3. Check "Always allow from this computer"
4. Click **OK**
5. Try installing again

### Issue: "INSTALL_FAILED_UPDATE_INCOMPATIBLE"

**Solution:**
Uninstall old version first:

```powershell
adb uninstall com.aithink.studycompanion
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Issue: "Gradle wrapper not found"

**Solution:**

1. Open project in Android Studio first
2. Let Gradle sync complete
3. Gradle wrapper files will be auto-generated
4. Try the script again

### Issue: "Build failed"

**Solution:**

1. Open project in Android Studio
2. Let it sync completely
3. Fix any errors shown in IDE
4. Try: Build → Clean Project
5. Then: Build → Rebuild Project

---

## 📊 Expected Results

### Build Output

```
BUILD SUCCESSFUL in 2m 30s
139 actionable tasks: 139 executed
```

### Installation Output

```
Performing Streamed Install
Success
```

### Launch Output

```
Starting: Intent { cmp=com.aithink.studycompanion/.ui.MainActivity }
```

---

## 🎯 Quick Reference Commands

### Check devices:

```powershell
adb devices
```

### Install APK:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Uninstall app:

```powershell
adb uninstall com.aithink.studycompanion
```

### Launch app:

```powershell
adb shell am start -n com.aithink.studycompanion/.ui.MainActivity
```

### View logs:

```powershell
adb logcat | Select-String "AiThinkApp|AIService"
```

### Take screenshot:

```powershell
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

---

## 🎉 After Installation

### What You'll See

1. **Login Screen** (first launch)
    - Enter your name and email
    - Select role: Student or IT Professional
    - Choose learning purpose
    - Click "Start Learning"

2. **Home Screen**
    - Shows "🎓 AiThink" logo
    - "AI Study Companion" subtitle
    - "Start Learning" button

3. **Bottom Navigation**
    - Home 🏠
    - Dashboard 📊
    - Subjects 📚
    - History 📜
    - Profile 👤

### Check Logs

To see what's happening behind the scenes:

```powershell
adb logcat | Select-String "AiThinkApp|AIService"
```

Look for:

- "✅ RunAnywhere SDK is available and ready" (SDK active)
- "⚠️ RunAnywhere SDK not available - using enhanced fallback mode" (Fallback mode)

---

## ✅ Success Checklist

- [ ] Emulator is running
- [ ] ADB can see the device (`adb devices` shows device)
- [ ] APK built successfully
- [ ] APK installed without errors
- [ ] App appears in emulator's app drawer
- [ ] App launches without crashing
- [ ] Login screen is visible

---

<div align="center">

## 🚀 Ready to Install!

**Fastest Method**:

1. Open project in Android Studio
2. Start emulator (Tools → Device Manager)
3. Click Run button (▶️)
4. Done!

**Alternative**: Build in Android Studio, then drag APK onto emulator

**Status**: ✅ All files complete | 🎯 Ready to install

</div>

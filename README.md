![Project cover](cover.png)

# bank-app – Android Banking Application
#### Video Demo: (coming soon)

## Description
bank-app is the Kotlin-based Android client for the Banking Platform, providing
a clean Gradle setup and modular structure for retail-banking use cases.

---

## Technologies Used
- Kotlin
- Android SDK, AndroidX (Jetpack)
- Gradle (Kotlin DSL)
- Material Components

---

## How to Run the Project

The debug build uses `http://10.0.2.2:8080/` by default for Android emulator
development. Debug HTTP is permitted only for the exact host `10.0.2.2`.
Physical-device and other custom development endpoints should use HTTPS with
`-PAPI_BASE_URL=...`. Release builds require an absolute HTTPS API base URL;
the current `https://api.example.invalid/` default is a syntax-safe placeholder,
not a reachable service.

Android app-private data is intentionally excluded from cloud backup and
standard device transfer. A restored or new device requires sign-in again, and
local app-lock and default-account preferences must be set up again. Bank-core
account and transaction data remains server-side and is available after
authentication. Android OEM migration tools may have behavior outside the
standard backup-rule contract.

The Gradle project targets Android API 36 extension 20 and Build Tools 36.1.0.

### 1. Clone the repo
```bash
git clone https://github.com/gimesha-adikari/bank-app.git
cd bank-app
```

### 2. Open in Android Studio
- File → Open… → select the project folder
- Let Gradle sync complete
- Choose an Android Virtual Device (AVD) or plug in a device with USB debugging

### 3. Build & Run
- Click **Run ▶** in Android Studio, or run from CLI:
```bash
ANDROID_HOME=/path/to/android-sdk ANDROID_SDK_ROOT=/path/to/android-sdk \\
  bash ./gradlew assembleDebug -PAPI_BASE_URL=http://10.0.2.2:8080/
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Why This Project?
To provide a focused, production-minded Android foundation for banking workflows, with a modern Kotlin/Gradle setup that’s easy to extend and maintain.

---

## Acknowledgments
- Android Developers documentation
- Kotlin language and tooling

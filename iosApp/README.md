# iOS app shell

This folder has the Kotlin-facing Swift source (`iosApp/iOSApp.swift`, `iosApp/ContentView.swift`, `iosApp/Info.plist`) ready to go, but **no `.xcodeproj`** — that project-shell file is Xcode's own generated format and isn't safe to hand-author outside Xcode.

On a Mac, to get this running:

1. Install Xcode, then either:
   - Use the JetBrains KMP wizard at https://kmp.jetbrains.com to generate a fresh `iosApp/` folder for a project named `MealPlannerapp` with package `com.example.mealplannerapp`, targeting the same Android+iOS combo — then copy the three files above into the generated `iosApp/iosApp/` folder, replacing the wizard's placeholders.
   - Or create a new Xcode "App" project (SwiftUI, Swift) named `iosApp` directly inside this `iosApp/` folder, then replace its generated `iOSApp.swift`/`ContentView.swift`/`Info.plist` with the ones here.
2. Add the `shared` Kotlin Multiplatform framework as a dependency: in Xcode, add a "Run Script" build phase that invokes `../shared/build/xcode-frameworks/gradlew embedAndSignAppleFrameworkForXcode` (or use the newer Kotlin Multiplatform Xcode plugin), or link `shared.framework` directly if you build it manually with `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.
3. Build and run on an iOS Simulator or device.

The Android app (`androidApp` module) has no such gap — it builds and runs today via `.\gradlew.bat :androidApp:installDebug`.

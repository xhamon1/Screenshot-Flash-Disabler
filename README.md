# Screenshot Flash Disabler

An LSPosed/Xposed module for Android that suppresses the white flash SystemUI shows when a screenshot is captured.

## How it works

The module hooks `com.android.systemui` and intercepts `View.setVisibility()` and `View.setAlpha()` on the flash overlay view (`screenshot_flash`), keeping it hidden and fully transparent.

## Requirements

- LSPosed (Zygisk or legacy)
- Android 14+ (API 34)

## Build

./gradlew assembleDebug

Output APK: app/build/outputs/apk/debug/

## Install

1. Build or download the APK and install it.
2. Open LSPosed Manager, enable the module, select System UI in its scope.
3. Reboot.

# Orbit World Time — Android Studio project

A native Kotlin Android app concept with a distinctive midnight-navy / mint interface, searchable IANA time zones, pinned clocks, and an Android home-screen widget.

## Features
- Searches the time-zone database available on the device (includes IANA zones for countries and territories worldwide; some countries have multiple zones).
- Pin/unpin any zone. The first four pinned entries are shown in the home-screen widget.
- Live time in the app and per-zone `TextClock` elements on the home-screen widget.
- No network permission or account required.

## Build the APK
This workspace does not have the Android SDK or Gradle installed, so it could not compile and sign an APK here. To build it:
1. Install Android Studio (with Android SDK 35).
2. Open this folder (`WorldTimeAndroid`) in Android Studio.
3. Let Gradle sync, then choose **Build > Build APK(s)**.
4. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
5. Install the APK on your Android phone. For a release APK, create your own signing key via **Build > Generate Signed Bundle / APK**.

## Widget setup
After installing, long-press an empty area of your Android home screen, choose **Widgets**, find **Orbit World Time**, and place it. Pin the desired time zones inside the app; the first four pinned clocks are used by the widget. Widget appearance and resizing options depend on the launcher.

## Notes
- The app uses the time-zone database on the phone. Exact availability of zone IDs can vary with Android version.
- Android launchers control home-screen widgets. The included widget supports four selected zones; the app can pin more, but only the first four are displayed in this widget layout.

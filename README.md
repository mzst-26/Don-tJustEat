# DontJustEat — Android App

## Project Overview

**DontJustEat** is an Android application for restaurant discovery, booking, notifications, and profile management.  
It integrates **Firebase** (Authentication, Firestore, Storage, Cloud Messaging) and **Google Maps** for location features.

If you are new to Android development, start with the [Quick Start Summary](#quick-start-summary) and follow the links in order.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Quick Start Summary](#quick-start-summary)
- [Firebase Setup](#firebase-setup)
  - [Get SHA-1](#get-sha1)
  - [Get SHA-256](#get-sha-256)
  - [Firebase Console Settings](#firebase-console-settings)
  - [Download google-servicesjson](#download-google-servicesjson)
- [Google Maps API Setup](#google-maps-api-setup)
- [Local Configuration](#local-configuration)
- [Build & Run](#build--run)
  - [Android Studio](#build--run-android-studio)
  - [Gradle CLI](#build--run-gradle-cli)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)
- [References](#references)

---

## Prerequisites

You will need:

- **JDK 17**
- **Android Studio** (Giraffe or newer recommended)
- **Android SDK** (via Android Studio)
- A **Google account** (Firebase + Maps setup)

If you don’t have Android Studio yet, see [References](#references).

---

## Quick Start Summary

- Create a Firebase project and register the Android app → [Firebase Setup](#firebase-setup)
- Get SHA-1 and SHA-256 keys → [Get SHA-1](#get-sha1) and [Get SHA-256](#get-sha-256)
- Add keys in Firebase Console → [Firebase Console Settings](#firebase-console-settings)
- Download and place `google-services.json` → [Download google-servicesjson](#download-google-servicesjson)
- Create a Google Maps API key → [Google Maps API Setup](#google-maps-api-setup)
- Set `MAPS_API_KEY` in `local.properties` → [Local Configuration](#local-configuration)
- Build & run → [Build & Run](#build--run)

---

## Firebase Setup

Firebase is required for Authentication, Firestore, Storage, and Messaging.

### Get SHA-1

Choose one method below.

#### Method A — Android Studio (recommended)

- Open the project in Android Studio
- Open the **Gradle** tool window
- Run the Gradle task:

  - `app` → `Tasks` → `android` → `signingReport`

- Copy the **SHA-1** value from the output window

#### Method B — Command line (debug keystore)

Run:

    keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore

**Default debug keystore password:** `android`

> Tip: If `~/.android/debug.keystore` doesn’t exist, run the project once in Android Studio to generate the debug keystore.

---

### Get SHA-256

Use the same method as SHA-1:

- Android Studio: `signingReport` output includes **SHA-256**
- Command line: the `keytool -list -v ...` output includes **SHA-256**

Copy the **SHA-256** value for Firebase.

---

### Firebase Console Settings

Open Firebase project settings:

- https://console.firebase.google.com/project/_/settings/general/

Then:

- Register an Android app with package name:

  - `com.example.dontjusteat`

- Add your fingerprints:

  - SHA-1 (from [Get SHA-1](#get-sha1))
  - SHA-256 (from [Get SHA-256](#get-sha-256))

- Save changes

---

### Download google-services.json

From the Firebase console (your app settings):

- Download `google-services.json`
- Place it here:

  - `app/google-services.json`

If you skip this step, the build will fail. See [Troubleshooting](#troubleshooting).

---

## Google Maps API Setup

Google Maps is required for location features.

- Open Google Cloud Console:
  - https://console.cloud.google.com/
- Create/select a project
- Enable **Maps SDK for Android**
- Create an **API key**
- Restrict the key to:
  - **Android app**
  - Package name: `com.example.dontjusteat`
  - SHA-1 fingerprint (from [Get SHA-1](#get-sha1))

Docs:
- https://developers.google.com/maps/documentation/android-sdk/start

---

## Local Configuration

The app expects a Maps API key in `local.properties` at the project root.

Add:

    MAPS_API_KEY=YOUR_KEY_HERE

This value is injected into the manifest at build time.

---

## Build & Run

### Build & Run (Android Studio)

- Open the project in Android Studio
- Let Gradle sync complete
- Select a device/emulator
- Click **Run**

### Build & Run (Gradle CLI)

Build:

    ./gradlew assembleDebug

Install to a connected device:

    ./gradlew installDebug

---

## Troubleshooting

- **Build fails with “google-services.json missing”**  
  → Ensure you completed [Download google-servicesjson](#download-google-servicesjson)

- **Maps not showing / API key error**  
  → Recheck [Local Configuration](#local-configuration) and API key restrictions

- **Firebase authentication errors**  
  → Confirm SHA-1 and SHA-256 were added in [Firebase Console Settings](#firebase-console-settings)

---

## FAQ

**Q: Can I run the app without Firebase?**  
A: No. The app relies on Firebase for authentication, data, and notifications.

**Q: What package name should I use in Firebase?**  
A: `com.example.dontjusteat`

---

## References

- Firebase Console Settings  
  https://console.firebase.google.com/project/_/settings/general/

- Google Maps SDK for Android  
  https://developers.google.com/maps/documentation/android-sdk/start

- Android Studio  
  https://developer.android.com/studio

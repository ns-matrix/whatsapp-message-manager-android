# WhatsApp Message Manager — Android companion

Android Studio project for the NX Matrix WhatsApp Message Manager mobile companion.

## Features

- Configure the HTTPS URL of your hosted Message Manager backend on first launch.
- Open the responsive web dashboard inside a native Android WebView.
- Retain dashboard cookies and session state on the device.
- Import CSV files through Android's native file picker.
- Change the backend URL from the in-app Settings control.
- Keep navigation inside the configured backend origin; external links open in the browser.

## Architecture and hosting

This Android app is a mobile dashboard client. WhatsApp Web authentication, QR pairing, contact storage, campaign processing, and message delivery run on the existing Node.js backend.

The backend must be deployed separately to a persistent host with Node.js 20+, the Chromium/Puppeteer dependencies required by `whatsapp-web.js`, HTTPS, persistent storage for the `data/` directory (especially `data/session`), and production secrets such as `APP_PASSWORD` and `SESSION_SECRET` configured as environment variables.

**GitHub Pages cannot run the Node.js/WhatsApp backend.** This repository stores the Android source and builds a debug APK through GitHub Actions.

## Build APK from GitHub

1. Open the **Actions** tab.
2. Select **Build Android APK**.
3. Choose **Run workflow**, or push a commit to `main` to trigger it automatically.
4. Open the completed workflow run and download the `message-manager-debug-apk` artifact.
5. Extract the artifact ZIP and install `app-debug.apk` on your Android device.

The workflow creates a debug APK for testing/sideloading. It is not a Play Store-signed release.

## Build locally

1. Install Android Studio and Android SDK Platform 35.
2. Open this repository as a project.
3. Allow Gradle sync to finish.
4. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## First launch

1. Deploy the existing Node.js backend to a persistent host.
2. Configure `APP_PASSWORD` and `SESSION_SECRET` securely on that host.
3. Ensure HTTPS and persistent storage are enabled.
4. Open the Android app and enter the backend's HTTPS URL.
5. Sign in to the dashboard and connect WhatsApp using its QR login flow.

### QR pairing note

If the QR code is displayed on the same Android phone that has WhatsApp installed, that phone generally cannot scan its own screen. Display the QR on a second device/computer, or use the official WhatsApp Business Platform for a supported production integration.

## Security

- Never commit `data/`, WhatsApp session files, passwords, cookies, or contact exports.
- Use HTTPS only and restrict backend access to trusted users.
- Use campaigns only with recipients whose consent has been recorded.
- The app does not bypass WhatsApp messaging limits or platform policies.

# Murmur Android

Murmur is an offline Android dictation keyboard. Hold **TALK**, speak, release, and the final text is inserted into the active input field through Android's `InputConnection`.

## Product boundary

This project contains exactly one speech engine and one fixed speech model: **Vosk small US English 0.15**. It is Apache-2.0 licensed, is 40 MB on disk, and is designed for Android. The app downloads it once during setup into private app storage, then dictation runs offline. No microphone audio is written to storage.

The model choice intentionally rejects Parakeet TDT v3 because its INT8 package is about 700 MB, over this project's 500 MB installed-footprint ceiling. Whisper base would fit but requires a larger native integration and has materially higher latency on lower-end phones. Vosk's published estimate is roughly 300 MB RAM at runtime.

## Build

Open this folder in Android Studio or AndroidIDE with JDK 17 and Android SDK Platform 35 installed, then run:

```sh
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## First use

1. Install the APK and open Murmur.
2. Tap **Install offline speech model** once (Wi-Fi recommended).
3. Allow microphone access.
4. Enable Murmur in Android's input-method settings and choose it in a text field.
5. Hold **TALK**, speak English, and release to insert text.

## Included

- Android `InputMethodService` with press-and-hold dictation
- 16 kHz mono microphone capture into RAM only
- offline Vosk streaming recognition with partial transcript display
- simple waveform HUD and recording state
- deterministic cleanup (whitespace, filler removal, duplicate words, capitalization)
- case-insensitive personal phrase dictionary
- local searchable SQLite history (no raw audio)

## Sources and licenses

- [Vosk Android](https://github.com/alphacep/vosk-api), Apache-2.0
- [Vosk model catalogue](https://alphacephei.com/vosk/models), model Apache-2.0
- Android [`InputMethodService`](https://developer.android.com/reference/android/inputmethodservice/InputMethodService) / `InputConnection` APIs

No source code was copied from GPL projects such as Outspoke.

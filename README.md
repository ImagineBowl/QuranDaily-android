# QuranDaily (Android)

Native Android app for reading and listening to the Quran daily. Built with **Kotlin** and **Jetpack Compose**, matching feature parity with the [QuranDaily iOS](https://github.com/ImagineBowl) app.

## Features

- **Read** — Surah list, Juz jump, continue reading, ayah-by-ayah reader with bookmarks
- **Listen** — Surah/ayah picker, recent listens, Read & Listen with ayah-by-ayah audio
- **Bookmarks** — Save ayahs, swipe to delete, open in reader
- **Settings** — Theme, Arabic/Urdu fonts, font size, storage info, cache clear, optional tip jar
- **Offline Quran** — One-time download of Arabic text + Urdu translation (AlQuran.cloud API)
- **Audio** — Shared mini player and full-screen player; stream or per-surah offline download
- **Background playback** — Media session and notification controls

No login. No paywall. Tips are optional only.

## Requirements

- Android **8.0+** (API 26+)
- Android Studio (recommended) with SDK **API 34+** for emulator testing
- JDK **17+** (bundled with Android Studio)

## Getting started

1. Clone the repository:

   ```bash
   git clone https://github.com/ImagineBowl/QuranDaily-android.git
   cd QuranDaily-android
   ```

2. Optional — if Gradle cannot find Java, copy the JDK hint file:

   ```bash
   cp jdk.local.properties.example jdk.local.properties
   ```

3. Open the project in Android Studio and **Sync Project with Gradle Files**.

4. Run the **`app`** configuration on a device or emulator (API 34+ recommended).

5. On first launch, tap **Download Quran Data** (requires internet). After that, all tabs are available offline for text.

### Git hooks (recommended)

Strip accidental Cursor co-author trailers from commits:

```bash
git config core.hooksPath .githooks
```

## Project structure

| Module | Role |
|--------|------|
| `:app` | Compose UI, navigation, ExoPlayer, Play Billing, manual DI (`AppContainer`) |
| `:core` | Domain models, use cases, repositories, JVM unit tests (`:core:test`) |

Package: `com.imaginebowl.qurandaily`

## Build from terminal

```bash
./gradlew :app:assembleDebug
./gradlew :core:test
```

Install on a connected device:

```bash
./gradlew :app:installDebug
```

## Play Console (optional)

Tip jar product IDs (consumables) are defined in code for when you configure Google Play:

- `com.imaginebowl.qurandaily.tip.small`
- `com.imaginebowl.qurandaily.tip.medium`
- `com.imaginebowl.qurandaily.tip.large`

Until products exist on an internal testing track, Settings shows fallback prices.

## License

MIT — see [LICENSE](LICENSE).

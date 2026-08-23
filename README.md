# Days Left Widget

A clean, minimalist, **free** home-screen widget that counts down the days left until what matters to you.

No ads. No tracking. No account. Just days.

## Features

- **Countdown cards** — create as many events as you like, each with its own target date
- **Home-screen widget** — a pure-black, OLED-friendly tile with a day-progress grid:
  bright dots for days still left, dim dots for days already lived
- **Adjustable window** — set both a start date and a target date to track true progress
- **Fully local** — your events never leave your device

## The Why

Last month I made a really huge jump from iOS to Android. There are pros and cons — but some apps never made the trip. In my case, an app called **LEFT**.

There are alternatives on the Play Store, but they aren't free. So I decided to create my own. First of all, I wanted to dive into Android development, and this felt like the perfect way to start!

Let's see how it will go. 🚀

## Download

Grab the latest APK from the [Releases](../../releases) page.

*Beta notice: current builds are debug-signed. Proper release signing comes later.*

## Build from Source

```bash
git clone https://github.com/daqwayne/days-left-widget.git
cd days-left-widget
./gradlew assembleDebug
```

Requires JDK 17 and Android SDK (API 35).

## Tech Stack

- Kotlin
- Jetpack Compose (app UI)
- Jetpack Glance (home-screen widget)
- Material 3 · java.time · Gson

## License

MIT © 2026 daqwayne — see [LICENSE](LICENSE).

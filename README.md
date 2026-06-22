# GoalGuard

> A productivity-focused Android launcher that connects daily habits to long-term life goals — with doom-scroll detection, focus sessions, and an optional AI coach.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Min SDK 24](https://img.shields.io/badge/minSdk-24-green.svg)](#)

GoalGuard replaces your home screen with a launcher that keeps your goals front and center. Track habits, run distraction-free focus sessions, and get nudged away from doom-scrolling — all stored **local-first**, with an optional cloud layer for sync and AI coaching.

---

## ✨ Features

- **Goal-driven home screen** — goal score ring combining goal progress and habit completion
- **Habit tracker** — daily completion toggles, streak counters, goal-linked habits
- **Focus sessions** — 15 / 30 / 60 / 90-minute timers with XP rewards
- **Doom-scroll detection** — usage-stats monitoring with an over-app intervention overlay
- **Onboarding + launcher setup** — guided permission flow to become the default home app
- **Insights & gamification** — screen-time charts and an XP/level system *(in progress)*
- **Optional cloud** — Google Sign-In, delta sync, and an AI coach — every cloud call degrades silently to the local path

## 🧱 Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.3.0 |
| Build | AGP / Gradle | 9.2.1 / 9.4.1 |
| UI | Jetpack Compose + Material 3 | BOM 2025.05.01 |
| Navigation | Navigation Compose (type-safe) | 2.9.0 |
| DI | Koin | 4.0.4 |
| Local DB | Room | 2.7.1 |
| Preferences | DataStore | 1.1.4 |
| HTTP | Ktor Client | 3.1.3 |
| Background | WorkManager | 2.10.1 |
| Multiplatform | Kotlin Multiplatform (`:shared`) | — |
| Backend | Ktor + Postgres/H2 (`:backend`) | — |

Min SDK 24 · Target/Compile SDK 36.

## 🏛️ Architecture

Clean Architecture + **MVI**, split into Gradle modules with convention plugins in `build-logic/`.

```
GoalGuard/
├── build-logic/        # Composite build — convention plugins
├── app/                # Entry point, DI wiring, navigation host
├── core/
│   ├── domain/         # Result<D,E>, DataError (shared types)
│   ├── data/           # HttpClientFactory, SafeCall helper
│   ├── database/       # Room database, DAOs, entities, converters
│   ├── design-system/  # Theme, color, type, shared composables
│   └── presentation/   # ObserveAsEvents, UiText, error mapping
├── feature/            # onboarding · dashboard · goals · habits ·
│                       # focus · insights · gamification · coach ·
│                       # guard · backup · auth
├── shared/             # Kotlin Multiplatform DTOs + API client
└── backend/            # Ktor server (auth, sync, coach, insights)
```

Each feature follows a three-layer split: `domain/` (entities, repository interfaces), `data/` (Room implementations, mappers), and `presentation/` (MVI `ViewModel` + `Screen` composable).

Every screen's `ViewModel` exposes a `StateFlow` of state, a `Flow` of one-time events, and a single `onAction()` entry point. See [`GOALGUARD.md`](GOALGUARD.md) for the full module graph, navigation flow, database schema, and DI breakdown.

## 🚀 Getting Started

### Prerequisites
- Android Studio (Ladybug or newer)
- JDK 17+
- Android SDK 36

### Build & run the app

```bash
git clone https://github.com/Shubhamgarg1072/GoalGaurd.git
cd GoalGaurd
./gradlew :app:assembleDebug      # or open in Android Studio and Run
```

`local.properties` (SDK location) is generated automatically by Android Studio and is intentionally **not** checked in.

### Run the backend (optional)

The app is fully functional without it. To run the cloud layer:

```bash
./gradlew :backend:run                                  # uses in-memory H2, zero infra
# or
docker compose -f backend/docker-compose.yml up --build  # Postgres
```

| Env var | Purpose | Default |
|---|---|---|
| `DB_URL` | Postgres connection | unset → in-memory H2 |
| `JWT_SECRET` | Token signing secret | dev value |
| `GOOGLE_WEB_CLIENT_ID` | Google ID-token verification | unset → insecure dev mode |
| `ANTHROPIC_API_KEY` | AI coach (Anthropic SDK) | unset → template coach |
| `COACH_MODEL` | Coach model id | `claude-opus-4-8` |

## 🤝 Contributing

Contributions are welcome! The `main` branch is protected — please:

1. Fork the repo (or create a branch if you're a collaborator)
2. Commit your changes
3. Open a Pull Request against `main`

Direct pushes to `main` are restricted to the maintainer.

## 📄 License

Released under the [MIT License](LICENSE). © 2026 Shubham Garg.

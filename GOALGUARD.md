# GoalGuard — Android App

A productivity-focused Android launcher that connects daily habits to long-term life goals, with doom-scroll detection and focus sessions.

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.3.0 |
| Build | AGP + Gradle | 9.2.1 / 9.4.1 |
| Annotation processing | KSP | 2.3.0 |
| UI | Jetpack Compose + Material 3 | BOM 2025.05.01 |
| Navigation | Navigation Compose | 2.9.0 |
| DI | Koin | 4.0.4 |
| Local DB | Room | 2.7.1 |
| Preferences | DataStore Preferences | 1.1.4 |
| HTTP | Ktor Client | 3.1.3 |
| Serialization | KotlinX Serialization | 1.8.1 |
| Coroutines | KotlinX Coroutines | 1.10.2 |
| Background | WorkManager | 2.10.1 |
| Java 8+ APIs | Core Library Desugaring | 2.1.5 |
| Splash | AndroidX SplashScreen | 1.0.1 |
| Min SDK | — | 24 |
| Target / Compile SDK | — | 36 |

---

## Architecture

Clean Architecture + MVI, split into Gradle modules.

```
GoalGuard/
├── build-logic/            # Composite build — convention plugins
│   └── convention/
│       ├── AndroidApplicationConventionPlugin
│       ├── AndroidLibraryConventionPlugin
│       ├── AndroidComposeConventionPlugin
│       ├── AndroidFeatureConventionPlugin
│       ├── AndroidKoinConventionPlugin
│       ├── AndroidRoomConventionPlugin
│       └── KotlinJvmConventionPlugin
│
├── app/                    # Entry point, DI wiring, navigation host
│
├── core/
│   ├── domain/             # Result<D,E>, DataError (shared types)
│   ├── data/               # HttpClientFactory, SafeCall helper
│   ├── database/           # Room database, DAOs, entities, type converters
│   ├── design-system/      # Theme, Color, Type, shared Composables
│   └── presentation/       # ObserveAsEvents, UiText, DataError extensions
│
└── feature/
    ├── onboarding/         # 3-step onboarding + launcher permissions setup
    ├── dashboard/          # Home screen — goal score, habits strip, time stats
    ├── goals/              # Goals list + goal detail with progress ring
    ├── habits/             # Daily habit tracker with streak counter
    ├── focus/              # Focus session timer (selector → active → done)
    ├── insights/           # Screen-time and productivity charts (stub)
    └── gamification/       # XP / level system (stub)
```

Each feature follows the same three-layer split:

```
feature/<name>/
  domain/       — entities, repository interfaces
  data/         — Room repository implementations, mappers
  presentation/ — ViewModel (MVI), Screen composable
```

---

## Module Graph

```
app
 ├── core:domain
 ├── core:data
 ├── core:database
 ├── core:design-system
 ├── core:presentation
 ├── feature:onboarding:domain
 ├── feature:onboarding:data  ──► core:database, core:domain
 ├── feature:onboarding:presentation ──► feature:onboarding:domain, feature:goals:domain
 ├── feature:dashboard:presentation ──► feature:goals:domain, feature:habits:domain
 ├── feature:goals:domain
 ├── feature:goals:data  ──► core:database, feature:goals:domain
 ├── feature:goals:presentation ──► feature:goals:domain
 ├── feature:habits:domain
 ├── feature:habits:data ──► core:database, feature:habits:domain
 ├── feature:habits:presentation ──► feature:habits:domain
 ├── feature:focus:domain
 ├── feature:focus:data ──► core:database, feature:focus:domain
 ├── feature:focus:presentation ──► feature:focus:domain
 ├── feature:insights:presentation
 └── feature:gamification:presentation
```

---

## Navigation Flow

```
Splash (SplashScreen API)
    │
    ├─► OnboardingRoute          (first launch — isOnboardingComplete = false)
    │       └─► LauncherPermissionsRoute   (after goal creation)
    │               └─► DashboardRoute
    │
    └─► DashboardRoute           (returning user — isOnboardingComplete = true)
            ├─► GoalDetailRoute(goalId)
            ├─► FocusRoute
            ├─► GoalsRoute  ────► GoalDetailRoute(goalId)
            ├─► HabitsRoute
            ├─► InsightsRoute
            └─► GamificationRoute
```

Routes are `@Serializable` objects / data classes (`Navigation 2.9.0` type-safe API):

```kotlin
@Serializable object OnboardingRoute
@Serializable object LauncherPermissionsRoute
@Serializable object DashboardRoute
@Serializable object GoalsRoute
@Serializable data class GoalDetailRoute(val goalId: String)
@Serializable object HabitsRoute
@Serializable object FocusRoute
@Serializable object InsightsRoute
@Serializable object GamificationRoute
```

---

## MVI Pattern

Every screen owns a `ViewModel` that exposes:

| Property | Type | Purpose |
|---|---|---|
| `state` | `StateFlow<XState>` | All UI state; collected with `collectAsStateWithLifecycle` |
| `events` | `Flow<XEvent>` | One-time side effects (navigation, toasts) via `Channel` |
| `onAction(XAction)` | `fun` | Single entry point for all user interactions |

Events are consumed in composables using `ObserveAsEvents` (wraps `LaunchedEffect` + `repeatOnLifecycle`).

---

## Screen Inventory

### Onboarding (`feature/onboarding`)
Three-step animated flow:
1. **Welcome** — app pitch, feature highlights
2. **Profile Setup** — name, age, occupation
3. **Create Goal** — goal name, target value, current value, priority

### Launcher Permissions (`feature/onboarding/presentation`)
Post-onboarding setup screen. Checks and requests four permissions with live status cards (auto-refreshes on `ON_RESUME`):

| Permission | Type | Method |
|---|---|---|
| Default Launcher | Required | `RoleManager.ROLE_HOME` (API 29+) / `ACTION_HOME_SETTINGS` |
| Usage Access | Required | `ACTION_USAGE_ACCESS_SETTINGS` |
| Display Over Apps | Required | `ACTION_MANAGE_OVERLAY_PERMISSION` |
| Notifications | Optional | `RequestPermission` contract (API 33+) |

### Dashboard (`feature/dashboard`)
- Goal score ring (combined goal progress + habit completion)
- Today's habits strip with toggle checkboxes
- Time stats row (Focus / Social minutes)
- Quick-launch cards → Focus, Goals, Habits, Insights

### Goals (`feature/goals`)
- Goals list with progress rings and priority badges
- Goal detail: description, progress bar, linked habits, target / current values

### Habits (`feature/habits`)
- Active habits list with daily completion toggles
- Streak counter per habit
- Frequency display (daily / weekday / custom)

### Focus Mode (`feature/focus`)
Three-state animated screen:
1. **Selector** — choose session duration (15 / 30 / 60 / 90 min)
2. **Active** — countdown timer with progress ring, abandon button
3. **Done** — XP earned badge, back to dashboard

### Insights (`feature/insights`)
Placeholder screen (screen-time charts, doom-scroll stats — to be wired).

### Gamification (`feature/gamification`)
Placeholder screen (XP, level, achievements — to be wired).

---

## Database Schema (Room)

### `goals`
| Column | Type |
|---|---|
| id | TEXT PK |
| title | TEXT |
| description | TEXT |
| targetValue | REAL |
| currentValue | REAL |
| unit | TEXT |
| priority | TEXT (GoalPriority enum) |
| deadline | TEXT (LocalDate) |
| createdAt | TEXT (LocalDate) |

### `habits`
| Column | Type |
|---|---|
| id | TEXT PK |
| title | TEXT |
| description | TEXT |
| frequency | TEXT (HabitFrequency enum) |
| linkedGoalId | TEXT nullable |
| createdAt | TEXT (LocalDate) |
| isActive | INTEGER (Boolean) |

### `habit_logs`
| Column | Type |
|---|---|
| id | TEXT PK |
| habitId | TEXT FK |
| completedDate | TEXT (LocalDate) |

### `focus_sessions`
| Column | Type |
|---|---|
| id | TEXT PK |
| durationMinutes | INTEGER |
| startedAt | TEXT (LocalDateTime) |
| completedAt | TEXT (LocalDateTime nullable) |
| wasCompleted | INTEGER (Boolean) |

`java.time` types are persisted as ISO strings via `@TypeConverters(DateConverters::class)`. `coreLibraryDesugaring` enables `java.time` on API < 26.

---

## Dependency Injection (Koin)

Modules registered in `GoalGuardApp.startKoin { ... }`:

| Module | Bindings |
|---|---|
| `DatabaseModule` | `GoalGuardDatabase` (singleton), all DAOs |
| `OnboardingModule` | `DataStoreOnboardingRepository → OnboardingRepository`, `OnboardingViewModel` |
| `GoalsModule` | `RoomGoalRepository → GoalRepository`, `GoalsViewModel`, `GoalDetailViewModel` |
| `HabitsModule` | `RoomHabitRepository → HabitRepository`, `HabitsViewModel` |
| `FocusModule` | `RoomFocusRepository → FocusRepository`, `FocusViewModel` |
| `DashboardModule` | `DashboardViewModel` |

Koin 4.0 repository binding syntax (lambda overload removed):

```kotlin
single<GoalRepository> { RoomGoalRepository(get()) }
```

---

## Manifest Permissions

| Permission | Type | Purpose |
|---|---|---|
| `INTERNET` | Normal | Future AI coaching / sync |
| `POST_NOTIFICATIONS` | Runtime (API 33+) | Habit reminders |
| `PACKAGE_USAGE_STATS` | Protected | Screen-time / doom-scroll detection |
| `QUERY_ALL_PACKAGES` | Normal (declare on API 30+) | App drawer enumeration |
| `SYSTEM_ALERT_WINDOW` | Special | Doom-scroll intervention overlay |
| `RECEIVE_BOOT_COMPLETED` | Normal | Restore WorkManager tasks on reboot |
| `EXPAND_STATUS_BAR` | Normal | Pull-down status bar from home screen |

The activity declares both `CATEGORY_LAUNCHER` and `CATEGORY_HOME` / `CATEGORY_DEFAULT` intent filters, making GoalGuard selectable as the system home screen.

---

## Build Configuration Notes

### AGP 9.x + KSP 2.3.0 compatibility

Two flags are required in `gradle.properties` until KSP adds native AGP 9.x built-in Kotlin support:

```properties
android.builtInKotlin=false
android.newDsl=false
```

These are deprecated and will be removed in AGP 10.0.

### KSP version format

From Kotlin 2.3.0 onwards, KSP uses a single-version format matching Kotlin:

```toml
ksp = "2.3.0"   # was "2.x.y-1.0.z" format before 2.3.0
```

### Convention plugins (`build-logic`)

All modules consume convention plugins via `alias(libs.plugins.goalguard.*)` instead of raw plugin IDs to avoid double-application errors. KSP is only applied in modules that need it (`:core:database`).

---

## Key Implementation Notes

### Splash screen / start destination

`MainActivity` uses `mutableStateOf` (not a plain `var`) for the ready flag so Compose tracks state changes and the `setKeepOnScreenCondition` lambda can dismiss the splash correctly:

```kotlin
var isReady by mutableStateOf(false)
splashScreen.setKeepOnScreenCondition { !isReady }
// ... async check ...
isReady = true   // triggers recomposition AND dismisses splash
```

### Focus session completion

Session completion is an in-screen state (`isSessionDone = true`) rather than a new route — avoids back-stack pollution. `OnDismissSessionDone` resets state then fires `NavigateBack`.

### Bottom nav visibility

The nav host hides the bottom bar on any route not in `topLevelRoutes`:

```kotlin
private val topLevelRoutes = setOf(
    DashboardRoute::class, GoalsRoute::class, HabitsRoute::class,
    InsightsRoute::class, GamificationRoute::class,
)
val showBottomBar = topLevelRoutes.any { currentDest?.hasRoute(it) == true }
```

This means Onboarding, LauncherPermissions, FocusMode, and GoalDetail all render full-screen without the bar.

### Ktor 3.x API changes

```kotlin
// Logger.DEFAULT removed — use level only
install(Logging) { level = LogLevel.ALL }

// defaultRequest contentType() removed — use header directly
defaultRequest {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
}
```

---

## Cloud (KMP shared module + Ktor backend)

The app stays **local-first**: cloud is strictly optional and every cloud call degrades silently
to the local path. Two new modules add Android↔backend code sharing and a server.

### `:shared` — Kotlin Multiplatform (`androidTarget` + `jvm`)

One set of `@Serializable` DTOs + the API client contract, consumed by **both** the Android app
and the backend, so the wire format can't drift. Uses **`kotlinx-datetime`** (commonMain can't use
`java.time`); the app converts at the mapper boundary (`cloud/CloudMappers.kt`).

```
shared/src/commonMain — model/ (Auth/Sync/Coach/Insights DTOs, ApiError),
                        api/ (GoalGuardApi, KtorGoalGuardApi, TokenProvider, HttpClientFactory),
                        util/ (Result, NetworkError, safeCall)
        androidMain    — defaultHttpEngine() = Ktor Android engine
        jvmMain        — defaultHttpEngine() = Ktor CIO engine
```

> AGP-9 note: a KMP `androidTarget` builds under the existing `android.builtInKotlin=false` /
> `android.newDsl=false` flags. `kotlin-multiplatform` is declared `apply false` in the root build.

### `:backend` — Ktor + Postgres (JVM only)

Ktor Netty server depending on `:shared` for its DTOs. Exposed + HikariCP over **Postgres**
(falls back to in-memory **H2** when `DB_URL` is unset, so it runs with zero infra).

| Area | Endpoint(s) |
|---|---|
| Auth | `POST /auth/google` (verify Google ID token → issue JWTs), `POST /auth/refresh`, `GET /me` |
| Sync | `POST /sync` — delta push/pull, **last-write-wins on `updatedAt`**; `habit_logs` union |
| Coach | `POST /coach/generate` — Anthropic Java SDK (`claude-opus-4-8`, `COACH_MODEL`-configurable), template fallback on any error |
| Insights | `GET /insights/summary?from=&to=` — server aggregates over synced data |

Run it: `docker compose -f backend/docker-compose.yml up --build`, or `./gradlew :backend:run`
(H2). Env: `DB_URL`, `JWT_SECRET`, `GOOGLE_WEB_CLIENT_ID` (unset = **insecure dev** token
verification), `ANTHROPIC_API_KEY` (unset = template coach), `COACH_MODEL`.

### Auth — Google Sign-In (no Firebase)

`:feature:auth` (`domain`/`data`/`presentation`) uses **Credential Manager + Sign in with Google**.
Flow: app gets a Google ID token → `POST /auth/google` → backend verifies with Google → issues app
JWTs → `DataStoreTokenStore` persists them (also the shared `TokenProvider`). The **Sign-In screen**
sits after Launcher Permissions in onboarding; **"Skip for now"** keeps the app fully local.

### Android cloud wiring (`:app`)

- `di/NetworkModule` — `createGoalGuardApi(BuildConfig.BASE_URL, tokenStore)` (Ktor hidden in shared).
- `di/AuthModule` — `GoogleAuthRepository`, `SyncRepository`, `SignInViewModel`.
- `cloud/SyncRepository` — reuses `BackupRepository.export()/importBundle()` + `/sync`.
- `cloud/RemoteCoachTextGenerator` — slotted as **primary** inside the existing
  `FallbackCoachTextGenerator` (fallback stays `TemplateCoachTextGenerator`).
- `BuildConfig.BASE_URL` defaults to `http://10.0.2.2:8080` (emulator→host); manifest sets
  `usesCleartextTraffic="true"` for local dev.

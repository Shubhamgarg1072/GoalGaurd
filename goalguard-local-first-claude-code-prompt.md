# GoalGuard — Local-First Build Spec (Claude Code Prompt)

> How to use this: paste **SHARED CONTEXT** once at the start of a Claude Code session.
> Then give it **PHASE 1**, let it finish and confirm the build is green, then **PHASE 2**, then **PHASE 3**.
> Do not paste all three phases at once — scope each one.

---

## SHARED CONTEXT (paste first, every session)

You are working in an existing Android app, **GoalGuard**, a local-first habit/goal coach
and launcher. Match the existing codebase conventions exactly — do not introduce new patterns.

**Stack (do not change versions):** Kotlin 2.3.0, Jetpack Compose + Material 3 (BOM 2025.05.01),
Navigation Compose 2.9.0 (type-safe `@Serializable` routes), Koin 4.0.4, Room 2.7.1,
DataStore Preferences 1.1.4, Ktor Client 3.1.3, KotlinX Serialization 1.8.1,
KotlinX Coroutines 1.10.2, WorkManager 2.10.1, core library desugaring 2.1.5. Min SDK 24,
target/compile SDK 36.

**Architecture:** Clean Architecture + MVI, split into Gradle modules under `core/` and `feature/`.
Each feature is `domain/` (entities, repo interfaces), `data/` (Room repo impls, mappers),
`presentation/` (MVI ViewModel + Compose Screen). Convention plugins live in `build-logic/` and
are consumed via `alias(libs.plugins.goalguard.*)` — never raw plugin IDs. KSP only in modules
that need it.

**MVI contract every ViewModel follows:**
- `state: StateFlow<XState>` (collected with `collectAsStateWithLifecycle`)
- `events: Flow<XEvent>` (one-time effects via `Channel`, consumed with `ObserveAsEvents`)
- `fun onAction(action: XAction)` (single entry point)

**Koin 4.0 binding syntax** (lambda overload is gone): `single<Repo> { RoomRepo(get()) }`.
Register feature modules in `GoalGuardApp.startKoin { ... }`.

**Shared types:** use the existing `Result<D, E>` and `DataError` from `core:domain`. Persist
`java.time` types as ISO strings via the existing `DateConverters`.

### NON-NEGOTIABLE ARCHITECTURAL RULES (the whole point of this app)
1. **The app must be fully functional with no network and no account.** Cloud is always optional.
2. **No cloud dependency on a critical path.** Any cloud feature degrades silently to a local
   fallback on error/unavailability. Never crash, never block UI, never show a hard error because
   a remote call failed.
3. **No Firebase / FCM / Firestore.** Notifications are scheduled locally. Backup is the user's own
   storage. (If a hosted LLM is ever added, it's behind one swappable interface — see Phase 1.)
4. **No secrets in the APK.** Never embed an API key.
5. Prefer adding nothing over adding a server. Justify any new module in one sentence.

Work incrementally. After each phase: the project must compile and existing screens must still work.
Write unit tests for pure logic (generators, mappers, detection rules). Do not modify unrelated code.

---

## PHASE 1 — `CoachTextGenerator` abstraction + Evening Summary / AI Coach screen

**Goal:** Implement the daily AI-coach text as a pure on-device template engine, behind an
interface that can later be swapped for an on-device model or a remote proxy without touching
callers. Then build the currently-missing Evening Summary screen on top of it.

**One-sentence justification:** the coach screen exists in the design but has no module; the
generator must be an interface so "real AI" is a later progressive enhancement, not a dependency.

### 1. In `core:domain`, add the contract (pure Kotlin, no Android imports):
```kotlin
data class CoachInput(
    val date: LocalDate,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val focusMinutes: Int,
    val socialMinutes: Int,
    val primaryGoalName: String,
    val primaryGoalPct: Int,
    val daysAheadOrBehind: Int,   // positive = ahead of schedule, negative = behind
    val topPendingHabit: String?, // null if all done
    val currentStreak: Int,
)

enum class CoachTone { CELEBRATORY, ENCOURAGING, NEUTRAL, GENTLE_NUDGE }

data class CoachMessage(val headline: String, val body: String, val tone: CoachTone)

interface CoachTextGenerator {
    suspend fun generate(input: CoachInput): CoachMessage
}
```

### 2. New module `feature:coach:domain` — depends on `core:domain`, `feature:goals:domain`,
`feature:habits:domain`, `feature:focus:domain`. Put `TemplateCoachTextGenerator` here:
- Pure, deterministic, offline, no rate limits. This is the **default** implementation.
- Pick tone from completion ratio + schedule delta (e.g. `>=0.8 && ahead` → CELEBRATORY,
  `<0.5` → GENTLE_NUDGE, else ENCOURAGING/NEUTRAL).
- Body interpolates the real numbers from `CoachInput`. Vary sentence templates so it doesn't
  read identically every day. Always reference the goal and the schedule delta. Mention
  `topPendingHabit` only when non-null.
- Add a `FallbackCoachTextGenerator(primary, fallback)` decorator that runs `primary`, and on
  ANY exception returns `fallback.generate(...)`. This is the seam for future Tier-2 generators.
  Leave the on-device-model and proxy implementations as TODO stubs in comments — do NOT implement
  them now and do NOT add Ktor/model dependencies in this phase.

### 3. New module `feature:coach:presentation` — depends on `feature:coach:domain` and the three
feature `domain` modules. Build:
- `CoachViewModel` (MVI). On load, assemble `CoachInput` by reading goals/habits/focus repos
  (today's logs, today's focus minutes, primary goal progress + schedule projection, streak),
  call `CoachTextGenerator.generate`, expose in state. Social minutes: read from a
  `ScreenTimeProvider` interface you define here with a `NoopScreenTimeProvider` returning 0
  for now (Phase 3 supplies the real one).
- `CoachScreen` composable matching the existing dark Material 3 theme and the Evening Summary
  design: date, 3 stat cards (habits X/Y, focus, social), the AI-coach message card, primary
  goal progress bar, 7-day streak bar strip. Full-screen, no bottom bar.

### 4. Wiring:
- Add `@Serializable object CoachRoute` and a composable destination. Reachable from the
  "More" menu and from a future evening-summary notification tap. Not a top-level (no bottom bar).
- `CoachModule` Koin: `single<CoachTextGenerator> { FallbackCoachTextGenerator(primary = TemplateCoachTextGenerator(), fallback = TemplateCoachTextGenerator()) }`
  and `viewModelOf(::CoachViewModel)`. Register in `startKoin`.
- Add convention-plugin-based `build.gradle.kts` for both new modules; wire module deps in `app`.

**Acceptance:** screen renders with real local data offline; tone changes correctly across inputs;
unit tests cover tone selection + interpolation; build green; no new third-party deps.

---

## PHASE 2 — Free backup/restore (user-owned storage, last-write-wins)

**Goal:** Let a user back up and restore all data with zero infra and no account we operate —
using a JSON export to their own storage (SAF), with an optional Google Drive App-Data folder
target. No Firebase.

**One-sentence justification:** users reinstall and get second phones; backup must cost us nothing
and keep the data in the user's hands.

### 1. Schema change (required for safe merge): add `updatedAt: Instant` to `goals`, `habits`,
and `focus_sessions` entities. Write a Room migration (bump version, add columns defaulting to
`createdAt`). `habit_logs` is append-only (unique on `habitId+completedDate`) — no `updatedAt`,
merged by union. Update DAOs to set `updatedAt = now()` on every write.

### 2. In `core:domain`, add the contract:
```kotlin
interface BackupRepository {
    suspend fun export(): Result<BackupBundle, DataError>
    suspend fun importBundle(bundle: BackupBundle): Result<Unit, DataError>
    suspend fun toJson(bundle: BackupBundle): String
    suspend fun fromJson(json: String): Result<BackupBundle, DataError>
}
```
`BackupBundle` is a `@Serializable` versioned DTO (`version: Int`, `createdAt: Instant`, and
serializable DTO lists for goals/habits/habitLogs/focusSessions). Keep DTOs separate from Room
entities; write mappers.

### 3. New module `core:backup` (data) — depends on `core:domain`, `core:database`. Implement
`RoomBackupRepository`:
- `export()` reads all tables → `BackupBundle`.
- `importBundle()` merges with **last-write-wins per row keyed on `updatedAt`** for goals/habits/
  focus_sessions (incoming row wins only if its `updatedAt` is newer); `habit_logs` merged by union
  (insert-or-ignore on the unique key). Do this in a single Room `@Transaction`. **Never** wipe and
  replace. Do NOT build CRDTs — LWW is correct for this single-user app.
- JSON via KotlinX Serialization.

### 4. UI in the existing Settings/More area:
- "Export backup" → SAF `ACTION_CREATE_DOCUMENT` (`application/json`), write `toJson(export())`.
- "Restore backup" → SAF `ACTION_OPEN_DOCUMENT`, read file, `fromJson` → `importBundle`.
- Show last-export timestamp (store in DataStore).
- Also add Android **Auto Backup**: set `android:allowBackup="true"` and an
  `android:fullBackupContent` / `dataExtractionRules` XML that includes the Room DB + DataStore.
  One manifest change, free, no code path.

### 5. OPTIONAL Drive App-Data target — implement only if straightforward, else leave a stubbed
`DriveAppDataBackupTarget` interface + TODO. If implemented: Google Sign-In with the
`drive.appdata` scope only, upload/download the same JSON bundle to the hidden appDataFolder via
REST (Ktor). It uses the user's own Drive quota. Must be entirely optional and behind the same
graceful-degradation rule — if Drive auth/network fails, fall back to file export.

**Acceptance:** export→wipe app data→restore round-trips all data; importing an older bundle does
NOT overwrite newer local rows; habit_logs never duplicate; migration runs clean; build green.

---

## PHASE 3 — Doom-scroll guard (foreground service + overlay), fully local

**Goal:** Detect doom-scrolling via `UsageStatsManager` and show the goal-tied intervention overlay,
running entirely on-device with deliberate battery behavior.

**One-sentence justification:** screen-time data only exists on the device and intervention must be
real-time, so this is inherently local and cannot be a cloud feature.

### 1. New module `feature:guard` (domain/data/presentation as needed):
- `UsageStatsReader` interface wrapping `UsageStatsManager` (so detection logic is unit-testable
  against fakes). Provides, per monitored package, current continuous foreground duration and
  recent open/switch counts.
- `ScreenTimeProvider` implementation (the real one Phase 1 stubbed): expose today's social-app
  minutes and focus minutes; bind it in Koin to replace `NoopScreenTimeProvider`.
- `DoomScrollDetector` (pure): given readings + config, returns a `Trigger?`. Rules from the PRD:
  continuous scrolling on a monitored app > threshold (default 15 min, configurable), repeated
  reopenings, rapid app switching. Thresholds + monitored package list stored in DataStore.

### 2. `DoomScrollGuardService` — a **foreground service** (ongoing notification, own low-priority
channel) that polls `UsageStatsReader` only while "Guard mode" is enabled. **Battery discipline
is part of the spec:**
- Register screen on/off receiver; **stop polling entirely when screen is off.**
- Only poll at the short interval (~5s) while a *monitored* app is foreground; otherwise back off
  to a long interval or idle.
- Reschedule/restart on `RECEIVE_BOOT_COMPLETED` (manifest permission already declared).

### 3. Intervention overlay via `SYSTEM_ALERT_WINDOW`:
- Add a window of type `TYPE_APPLICATION_OVERLAY` hosting a Compose UI (use a `ComposeView` with a
  lifecycle/saved-state owner wired up for a non-Activity window).
- Content matches the design's intervention modal: "You've spent N minutes scrolling," the goal tie
  ("Your goal is to buy a house by Dec 2028…"), pending habits list, and two actions —
  **Start Habit** (dismiss overlay, deep-link into the habit flow) and **Continue Scrolling**
  (dismiss, snooze re-trigger for a cooldown window).
- Guard against re-showing within a cooldown; respect a per-day cap.

### 4. Permissions UX: this module must check/request `PACKAGE_USAGE_STATS`
(`ACTION_USAGE_ACCESS_SETTINGS`) and overlay (`ACTION_MANAGE_OVERLAY_PERMISSION`) and degrade
gracefully if not granted (Guard simply stays off — never crash). Reuse the existing
LauncherPermissions status-card pattern.

**Notes / cautions to honor:**
- Prefer `UsageStatsManager` polling over an `AccessibilityService` (lighter, less review risk).
- Keep 
- all detection logic pure and unit-tested; the service is a thin shell around it.

**Acceptance:** with permissions granted and Guard on, exceeding the threshold on a monitored app
shows the overlay with correct goal/habit data; screen-off stops polling; denied permissions leave
the app fully usable; detector logic has unit tests; build green.

---

## Suggested cross-cutting follow-ups (do NOT do unless asked)
- Local notifications (habit reminders via `AlarmManager.setExactAndAllowWhileIdle`; evening-summary
  + streak alerts via WorkManager) with per-type channels.
- Home-screen widgets (`AppWidgetProvider` + `RemoteViews`, WorkManager periodic update + immediate
  broadcast on habit completion).
- Tier-2 coach: on-device model (MediaPipe LLM Inference / Gemini Nano) and/or a single stateless
  Cloudflare Worker proxy to a free LLM tier — both slotted behind `FallbackCoachTextGenerator`,
  never embedding a key in the APK.

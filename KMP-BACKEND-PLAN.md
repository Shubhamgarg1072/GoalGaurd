# GoalGuard — KMP Shared Module + Ktor/Postgres Backend Plan

> Scope: **Android + Backend only.** KMP payoff = one set of Kotlin DTOs + API contract
> shared by the app and the server, so the wire format can't drift. No iOS/desktop, no
> Compose Multiplatform.

---

## Guiding constraint

The project's **non-negotiable local-first rules stay intact**:

- The backend is **strictly optional**.
- Every cloud call **degrades silently to the local path** on any failure.
- No feature is moved onto a network-critical path.

The app remains fully functional with no network and no account. Signing in unlocks cloud
sync / coach / insights but is never required.

---

## The one real "gotcha" up front

`commonMain` can't use `java.time` (used today in `Goal`, `BackupBundle`, `DateConverters`, etc.).

- Shared API DTOs will use **`kotlinx-datetime`** instead.
- Existing `java.time` domain/Room code stays **untouched**; conversion happens at the
  mapper boundary (same pattern as the current `GoalMapper`).
- **Not** rewriting the ~30 existing modules — UI / Room / DataStore / Koin stay native.

---

## Authentication: Google Sign-In (no Firebase)

Consistent with the "No Firebase / FCM" rule, sign-in uses **Credential Manager + Sign in
with Google** (the `googleid` library), not Firebase Auth.

Flow:

```
[Sign-In screen]  "Continue with Google"
        │  Credential Manager → Google ID token (JWT from Google)
        ▼
[Backend]  POST /auth/google { idToken }
        │  verifies idToken signature + audience (client ID) with Google
        │  upserts user (email, name, picture)
        ▼
        returns { user, accessToken, refreshToken }   ← our own app JWTs
        │
        ▼
[App]  stores tokens in DataStore; all cloud calls use Bearer accessToken
```

- The Google **Web client ID** is used as the ID-token audience (configured in
  `BuildConfig` on the app, env var on the server). No secret embedded in the APK.
- Backend trusts only ID tokens it can verify against Google's public keys.

---

## New structure (additive)

```
:shared                      ← NEW  KMP (androidTarget + jvm). DTOs + API client contract. commonMain.
:backend                     ← NEW  Kotlin/JVM Ktor server app. Depends on :shared. (no Android)
:feature:auth:domain         ← NEW  AuthRepository interface, AuthState/User model
:feature:auth:data           ← NEW  Google sign-in (Credential Manager) + token store (DataStore)
:feature:auth:presentation   ← NEW  Sign-In screen (MVI) + AuthViewModel
app                          ← consumes :shared + :feature:auth, wires sign-in into nav
```

---

## Phases

Each phase ends compiling & green before the next begins.

### Phase 0 — Build foundations

- Add to version catalog: `kotlin-multiplatform` plugin, `kotlinx-datetime`,
  `ktor-server-*` (core / netty / content-negotiation / auth-jwt / status-pages),
  `exposed`, `postgresql`, `hikari`, `logback`,
  `androidx-credentials` (+ `credentials-play-services-auth`),
  `googleid` (com.google.android.libraries.identity.googleid),
  `google-api-client` (server-side ID-token verification).
- **Spike (top risk):** confirm a KMP `androidTarget` builds under the existing AGP 9 flags
  (`android.builtInKotlin=false` / `android.newDsl=false`). If it fights AGP 9, fallback is
  a plain `jvm()` + `android-library` pair for `:shared`. Verify before going deep.

### Phase 1 — `:shared` module (no app behavior change)

- DTOs in `commonMain`:
  - `AuthDtos` (`GoogleSignInRequest { idToken }`, `AuthResponse { user, accessToken, refreshToken }`, `RefreshRequest`, `UserDto`)
  - `SyncDtos` (Goal / Habit / HabitLog / FocusSession using `kotlinx-datetime`)
  - `CoachInputDto` / `CoachMessageDto`
  - `InsightsDtos`
  - `ApiError` envelope
- `GoalGuardApi` interface + `KtorGoalGuardApi` impl (Ktor client, bearer auth + token
  refresh) — reuses existing `safeCall` / `Result` / `DataError` style.

### Phase 2 — `:backend` Ktor + Postgres

- Ktor server: ContentNegotiation(json), JWT auth, StatusPages.
- Exposed + Hikari + Postgres tables: `users`, `goals`, `habits`, `habit_logs`,
  `focus_sessions` (with `user_id` + `updated_at`), `refresh_tokens`.
- Endpoints:

  | Area | Endpoints |
  |---|---|
  | **Auth** | `POST /auth/google` (verify Google ID token → issue JWTs), `POST /auth/refresh`, `GET /me` |
  | **Sync** | `POST /sync` — delta push/pull, **last-write-wins on `updatedAt`** (server-backed version of the backup merge; `habit_logs` union) |
  | **Coach** | `POST /coach/generate` — LLM server-side (key stays on server), falls back to template on error |
  | **Insights** | `GET /insights/summary` — server aggregates over synced data |

- Config via env (`DB_URL`, `JWT_SECRET`, `GOOGLE_WEB_CLIENT_ID`, `ANTHROPIC_API_KEY`).
- `Dockerfile` + `docker-compose.yml` (Postgres) for local run.
- Coach LLM call: consult the `claude-api` skill; default to a cost-appropriate model
  (e.g. Haiku 4.5).

### Phase 3 — Android sign-in + optional-cloud wiring (graceful)

- **`:feature:auth` module set:**
  - `domain`: `AuthRepository`, `AuthUser`, `AuthState`.
  - `data`: `GoogleAuthRepository` using Credential Manager → ID token → `POST /auth/google`;
    `DataStoreTokenStore` for access/refresh tokens.
  - `presentation`: **Sign-In screen** (MVI) — app pitch + "Continue with Google" button,
    loading/error states, "Skip for now" (stays local-only). `AuthViewModel`.
- Navigation: add `@Serializable object SignInRoute`; reachable from onboarding/Settings.
  Sign-in is **optional** — skipping leaves the app fully local.
- `SyncRepository` reusing existing `BackupRepository.export()` / `importBundle()`;
  manual "Sync now" in Settings + optional WorkManager periodic.
- `RemoteCoachTextGenerator(api)` slotted as **primary** inside the existing
  `FallbackCoachTextGenerator` (fallback stays `TemplateCoachTextGenerator`) — exactly the
  seam the spec left.
- Koin wiring; base URL + Google Web client ID via `BuildConfig`. Every path degrades
  silently to local on failure.

### Phase 4 — Tests & docs

- Shared DTO serialization round-trips.
- Backend route tests (`testApplication`), including a faked Google token verifier.
- LWW merge unit tests; `AuthViewModel` tests with a fake `AuthRepository`.
- Update `GOALGUARD.md`.

---

## Open confirmation

- **Backend local run** — assume Docker for Postgres (`docker-compose up`), or also provide
  an H2/in-memory fallback profile? (Default: Docker + Postgres.)

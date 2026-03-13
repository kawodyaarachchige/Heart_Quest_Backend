# Week 8 Presentation Form — Heart Quest  
**CIS045-3 Distributed Service Architectures**

*Please answer the questions below, print this form and bring it to the presentation where it will be checked against reality and signed off by the tutor. Once signed off, scan this form and submit on BREO with all other deliverables (video, code). At this stage, Week 8, (at least) a working prototype should be presented plus some ideas on how to continue with the development.*

---

## 1. Software Architecture (Low Coupling / High Cohesion)  
**How is your code structured? (e.g., components, files, libraries, classes, packages …)**

**Frontend (Angular):**
- **Pages:** Splash, Auth, Forgot Password, Home, Start, Game, Results, Leaderboard — each in its own component (HTML, TS, SCSS).
- **Shared components:** `PageHeaderComponent`, `PanelComponent` — reused across pages for consistent layout and panels.
- **Services:** `BackendApiService` (HTTP to our backend), `ForgotPasswordService`, `GameStateService`, `IdentityService`, `LeaderboardService` — each has a single responsibility; components depend on services, not on HTTP directly (low coupling).
- **Models:** `heart.models.ts` (e.g. `LeaderboardEntry`, `RoundResult`, `PlayerIdentity`).
- **Routing:** `app.routes.ts`; `auth-interceptor.ts` handles 401 and redirects to login.

**Backend (Java / Javalin):**
- **Packages:** `controller` (HTTP only), `service` (business logic), `repository` (data access), `client` (external API), `domain`, `dto`, `exception`, `util`.
- **Controllers:** `AuthController`, `GameController`, `LeaderboardController` — parse request, call service, return response (no DB or business logic in controllers).
- **Services:** `AuthService` (register, login, reset password), `GameService` (puzzle fetch, submit, skip, timeout), `ScoreCalculator` — contain rules and orchestration.
- **Repositories:** `UserRepository` / `SqliteUserRepository`, `ScoreRepository` / `SqliteScoreRepository` — all database access is in repositories (high cohesion, low coupling with rest of app).
- **Client:** `HeartApiClient` implements `PuzzleProvider` — single place that talks to the external Heart API.

**Separation of concerns:** Controllers → Services → Repositories; frontend components → services → API. No business logic in controllers or repositories beyond data mapping.

---

## 2. Event Driven Architectures  
**What in your code triggers events? (e.g., GUI, buttons, timeout …)**

- **GUI / buttons:** Login, Sign up, Forgot password (Continue, Reset password, Back), Start game (mode selection), Submit answer, Skip, Back to home, Next round, New game, View Leaderboard, Logout, password show/hide toggles — all use `(click)` event handlers in Angular templates.
- **Keyboard:** Enter key submits or continues on auth and forgot-password forms via `(keyup.enter)`.
- **Async / reactive events:** HTTP responses handled via RxJS `Observable.subscribe()` (e.g. `api.login(...).subscribe(...)`, `api.getPuzzle().subscribe(...)`, `api.submit(...).subscribe(...)`); timer for round countdown using `interval(1000).subscribe(...)` in the game component.
- **Navigation / routing:** Route changes trigger component load and lifecycle (e.g. `ngOnInit` fetches leaderboard or checks auth).
- **Backend:** Incoming HTTP requests trigger Javalin route handlers (e.g. POST `/api/auth/login` → `AuthController` → `AuthService.login`); no custom event bus — request/response is the event flow.

---

## 3. Interoperability  
**You are expected to use the API available via marcconrad.com/uob/heart/api.php**  
**What protocol do you use? (e.g., JSON, base64)**

- **With the Heart API (marcconrad.com):** The **backend** calls `https://marcconrad.com/uob/heart/api.php?out=json&base64=yes` over **HTTP/HTTPS**. The API returns **JSON** with fields `question` (base64-encoded image), `solution` (integer), `carrots` (integer). We use **JSON** for the response and **base64** for the image data; the backend parses JSON and passes puzzle data to the game logic and then to the frontend via our own API.
- **Frontend ↔ our backend:** **HTTP** with **JSON** request/response bodies (e.g. `Content-Type: application/json`). Endpoints include `/api/auth/login`, `/api/auth/register`, `/api/auth/me`, `/api/game/new`, `/api/game/puzzle`, `/api/game/submit`, `/api/leaderboard`, etc. Credentials sent with cookies (`withCredentials: true`).
- **Summary:** Protocol with external Heart API: **HTTP(S), JSON, base64**. Protocol between Angular app and our backend: **HTTP, JSON, session cookies**.

---

## 4. Virtual Identity  
**How did you implement virtual identity in your code? (e.g., Passwords, Cookies, IP Numbers …)**

- **Passwords:** Users register and log in with username and password. Passwords are **hashed** (SHA-256) on the backend before storage in SQLite; plain text is never stored. Forgot-password flow: user enters username, then new password; backend finds user (case-insensitive), hashes new password, updates DB via `UserRepository.updatePassword`.
- **Cookies / sessions:** After successful login, the backend creates an **HTTP session** (Javalin/Jetty) and stores `userId` and `username` in the session; the server sends a **session cookie** to the browser. Subsequent requests (e.g. `/api/auth/me`, `/api/game/submit`) send the cookie; backend uses `ctx.req().getSession(false)` and `SessionUtils.requireLogin(ctx)` to enforce authenticated routes. Logout invalidates the session.
- **Where in code:** `AuthController` — `ctx.req().getSession(true)`, `session.setAttribute("userId", user.id())`, `session.setAttribute("username", user.username())`; `SessionUtils.requireLogin(ctx)` for protected routes; `AuthService` hashes passwords and uses `UserRepository` for persistence. Frontend uses `withCredentials: true` so the browser sends the session cookie on API calls; `auth-interceptor` redirects to login on 401.
- **No IP-based identity:** Virtual identity is based on username/password and server-side session (cookie), not IP.

---

## 5. Any other interesting features

- **Forgot password / reset password:** Full flow (enter username → set new password → confirm) with validation and error messages; case-insensitive username lookup so reset works regardless of casing.
- **Leaderboard:** Global top scores with ranking; results page shows “Top 10” and highlights current player; dedicated leaderboard page with podium and list.
- **Timed game mode:** Round timer with on-screen countdown; automatic timeout sent to backend; score calculation (including carrots hint) on backend.
- **Single database:** One SQLite DB under `heart-quest-backend/data/heartquest.db`; backend defaults to this path whether run from project root or backend directory (no duplicate DBs).
- **Shared UI theming:** Reusable panels, buttons, typography, and layout (SCSS); gaming-style fonts and accent colours; responsive layout.
- **Unit tests (backend):** e.g. `CredentialsValidatorTest`, `ScoreCalculatorTest` in the Java project.

---

**Tutor sign-off (to be completed at presentation):**

| Checked against code/demo | Signed | Date |
|---------------------------|--------|------|
|                          |        |      |

---

*Once signed, scan this form and submit on BREO with your video and code.*

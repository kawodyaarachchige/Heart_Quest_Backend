# Heart Quest

A full-stack puzzle game built for **CIS045-3 Distributed Service Architectures**. The project demonstrates layered architecture, event-driven design, interoperability with an external API, and virtual identity management.

---

## Table of contents

- [Project structure](#project-structure)
- [Design principles](#design-principles)
- [Distributed architecture](#distributed-architecture)
- [API](#api)
- [How to use this project](#how-to-use-this-project)

---

## Project structure

The repository has **three main parts**:

```
HeartGame/
├── heart-quest/           # Angular front-end
├── heart-quest-backend/   # Java (Javalin) backend
└── README.md
```

### Front-end (`heart-quest/`)

| Folder / file       | Purpose |
|---------------------|--------|
| `src/app/pages/`   | One component per screen: auth, forgot-password, home, game, results, leaderboard |
| `src/app/shared/`  | Reusable components: `page-header`, `panel` |
| `src/app/services/`| `BackendApiService` (HTTP to backend), `GameStateService` (game state), auth and leaderboard helpers |
| `src/app/models/`  | TypeScript types: `GameMode`, `HeartPuzzle`, `RoundResult`, `LeaderboardEntry`, etc. |
| `src/app/app.routes.ts` | Route definitions |
| `proxy.conf.json`  | Proxies `/api` → backend (localhost:7070), `/uob/heart` → marcconrad.com |

The front-end **only communicates with the backend** via HTTP API requests. It does not access the database.

### Backend (`heart-quest-backend/`)

| Folder / file       | Purpose |
|---------------------|--------|
| `controller/`       | HTTP layer: `AuthController`, `GameController`, `LeaderboardController` — handle requests and call services |
| `service/`         | Business logic: `AuthService`, `GameService`, `ScoreCalculator` — no direct DB or HTTP details |
| `repository/`      | Interfaces for data access: `UserRepository`, `ScoreRepository` |
| `repository/sqlite/` | SQLite implementations: `SqliteUserRepository`, `SqliteScoreRepository`, `SqliteMigration` (schema) |
| `domain/`          | Core concepts: `User`, `Puzzle`, `GameSession`, `Score`, `LeaderboardEntry` — used across layers |
| `dto/`             | Request/response shapes: `LoginRequest`, `RegisterRequest`, `SubmitRequest`, etc. |
| `client/`          | `HeartApiClient` — calls external API (marcconrad.com/uob/heart/api.php) for puzzles |
| `main/Application.java` | Wires repositories → services → controllers and starts the Javalin server |

### Database

- **SQLite** file, by default: `heart-quest-backend/data/heartquest.db`
- Created and migrated automatically when the backend starts (`SqliteMigration`).
- Stores users (and hashed passwords) and scores (leaderboard).

---

## Design principles

### 1. Layered architecture (Controller → Service → Repository)

Each layer has a single responsibility and talks only to the next:

- **Controller** — Receives HTTP requests, parses body (e.g. JSON), calls a service, returns HTTP response.
- **Service** — Contains business logic (e.g. validate password, compute score). Uses repositories; does not touch the database directly.
- **Repository** — Reads and writes the database. Implementations (`SqliteUserRepository`, `SqliteScoreRepository`) use JDBC and SQL.

Example for login: `AuthController` receives the request → `AuthService` validates and creates session → `UserRepository` loads the user from the database.

### 2. Domain vs DTO

- **`domain/`** — Core business types (`User`, `Puzzle`, `GameSession`, etc.) used inside the backend. No HTTP or persistence details.
- **`dto/`** — Shapes of API request/response bodies (`LoginRequest`, `RegisterRequest`, etc.). Controllers convert between DTOs and domain objects so the API contract is separate from internal models.

### 3. Event-driven front-end

The Angular app reacts to:

- **User events** — Button clicks (login, submit, skip, keypad), form submissions, logout.
- **Time events** — 20-second round timer; on timeout the app calls the backend, applies the result, and loads the next puzzle (no navigation to results).
- **Network events** — HTTP responses (e.g. login success → navigate to home; 401 → auth interceptor clears session and redirects to login).

### 4. Single API entry point for the front-end

All server communication goes through `BackendApiService`. The front-end never talks to the database or to the external Heart API directly; only the backend does.

### 5. External API behind the backend

The backend uses **HeartApiClient** to call `https://marcconrad.com/uob/heart/api.php?out=json&base64=yes`. Protocol: **JSON** and **base64** for the puzzle image. The front-end receives puzzles via `GET /api/puzzle` from our backend.

---

## Distributed architecture

Heart Quest is a **distributed system** in the sense of separate processes and clear boundaries:

| Tier         | Technology   | Role |
|-------------|--------------|------|
| **Client**  | Angular      | Runs in the browser; UI and user actions; calls backend over HTTP only |
| **Server**  | Java Javalin | Runs on host (e.g. localhost:7070); auth, game logic, sessions; talks to DB and external API |
| **Database** | SQLite      | File-based storage; users and scores; only the backend accesses it |
| **External API** | marcconrad.com | Puzzle provider; only the backend calls it |

**Communication:**

- **Browser ↔ Backend:** REST over HTTP, JSON bodies, session cookie for identity. Front-end uses `withCredentials: true` so the cookie is sent. Proxy sends `/api` to the backend.
- **Backend ↔ Database:** JDBC (SQLite). Repositories encapsulate all SQL.
- **Backend ↔ External API:** HTTP GET to Heart API; JSON + base64 response.

So the **front-end does not access the database or the external API**; it only talks to our backend. This keeps a clear distributed boundary and makes security and changes easier (e.g. swap DB or API without touching the front-end).

---

## API

### Backend REST API (Heart Quest server)

All endpoints are under the base URL of the backend (e.g. `http://localhost:7070`). The front-end uses the proxy so `/api` is sent to the backend. Requests that require a logged-in user must include the session cookie (`withCredentials: true`). Request and response bodies are **JSON**.

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/health` | Health check; returns `{ "ok": true }`. |
| POST   | `/api/auth/register` | Register: body `{ "username", "password" }`. Returns `{ "ok": true }`. |
| POST   | `/api/auth/login` | Login: body `{ "username", "password" }`. Sets session cookie; returns `{ "id", "username" }`. |
| GET    | `/api/auth/me` | Current user (requires session). Returns `{ "id", "username", "bestScore", "totalGamesPlayed" }`. 401 if not logged in. |
| POST   | `/api/auth/logout` | Logout; invalidates session. Returns `{ "ok": true }`. |
| POST   | `/api/auth/forgot-password` | Request password reset: body `{ "username" }`. |
| POST   | `/api/auth/reset-password` | Set new password: body `{ "username", "newPassword" }`. |
| POST   | `/api/game/new` | Start a new game (requires session). Body `{ "mode": "timed" \| "practice" }`. Returns game state. |
| GET    | `/api/puzzle` | Get next puzzle for current game (requires session). Returns `{ "puzzleId", "imageDataUrl", "carrots" }` (puzzle is fetched from external Heart API by the backend). |
| POST   | `/api/round/submit` | Submit answer: body `{ "puzzleId", "answer" }`. Returns round result. |
| POST   | `/api/round/skip` | Skip puzzle: body `{ "puzzleId" }`. Returns round result (−10 score). |
| POST   | `/api/round/timeout` | Report timeout: body `{ "puzzleId" }`. Returns round result (−25 score); front-end then loads next puzzle. |
| GET    | `/api/leaderboard` | Top scores. Returns array of `{ "username", "bestScore", "updatedAtIso" }`. |

### External API (puzzle provider)

The backend fetches puzzles from the **Heart API**:

- **URL:** `https://marcconrad.com/uob/heart/api.php?out=json&base64=yes`
- **Method:** GET
- **Protocol:** Response is **JSON** with a **base64**-encoded image in the `question` field, plus `solution` and `carrots`. The backend maps this to the format used by the front-end (e.g. `imageDataUrl`, `puzzleId`).

The front-end does not call this URL; only the backend does (see `HeartApiClient` in `heart-quest-backend/.../client/HeartApiClient.java`).

---

## How to use this project

### Prerequisites

- **Node.js** (v18+ recommended) and **npm** — for the Angular front-end
- **Java 21** and **Maven** — for the backend

### 1. Start the backend

From the project root:

**Option A — Run from your IDE**  
Run the main class `uob.cis045.heartquest.Main` (it delegates to `Application.main`).

**Option B — Build and run with Maven + Java**

```bash
cd heart-quest-backend
mvn package -q
java -jar target/heart-quest-backend-1.0.0.jar
```

The server will listen on **http://localhost:7070**.

**Optional environment variables:**

| Variable     | Default | Description |
|-------------|---------|-------------|
| `HQ_PORT`   | `7070`  | Port the backend listens on |
| `HQ_DB_PATH`| `data/heartquest.db` (relative to backend dir) | Path to the SQLite database file |

Example with custom port and DB path:

```bash
export HQ_PORT=8080
export HQ_DB_PATH=/path/to/my/heartquest.db
cd heart-quest-backend && mvn compile exec:java -q -Dexec.mainClass="uob.cis045.heartquest.Main"
```

### 2. Start the front-end

From the project root, in a **separate terminal**:

```bash
cd heart-quest
npm install
npm start
```

This runs `ng serve --proxy-config proxy.conf.js`. The app will be at **http://localhost:4200/**.

The proxy sends:

- `/api` → `http://localhost:7070` (backend)
- `/uob/heart` → `http://marcconrad.com` (external Heart API; in this project the backend fetches puzzles, so the front-end only uses `/api`)

The Angular app must run with this proxy so `/api` requests and cookies work correctly.

### 3. Use the application

1. Open **http://localhost:4200/** in a browser.
2. Register or log in (session is stored via cookie).
3. On the **home** page, read **How to play** (scoring: correct adds points; wrong −25; skip −10; timeout −25).
4. Start a game (timed or practice). Puzzles are fetched by the backend from the external Heart API.
5. Play rounds (submit answer, skip, or wait for timeout in timed mode — timeout loads the next puzzle automatically).
6. View results after submit/skip; view the leaderboard (data from the backend and SQLite).

### 4. Run backend tests

```bash
cd heart-quest-backend
mvn test
```

Uses JUnit Jupiter (see `pom.xml`). Tests live under `src/test/java` (e.g. `CredentialsValidatorTest`, `ScoreCalculatorTest`).

### 5. Build front-end for production

```bash
cd heart-quest
npm run build
```

Output is in `heart-quest/dist/`. For production you would serve these files and point the app’s API base URL to your deployed backend (and configure CORS/cookies as needed).

---

## Summary

| Topic        | Summary |
|-------------|---------|
| **Structure** | Angular app in `heart-quest/`, Javalin backend in `heart-quest-backend/`, SQLite DB under backend `data/`. |
| **Design**   | Layered backend (Controller → Service → Repository); domain vs DTO; event-driven front-end; single API service. |
| **Distributed** | Client (browser) ↔ Backend (Javalin) ↔ DB (SQLite); backend ↔ External Heart API. Front-end does not touch DB or external API. |
| **API**      | Backend REST API under `/api` (auth, game, round, leaderboard); external Heart API at marcconrad.com for puzzles (JSON + base64). |
| **Run**      | Start backend (port 7070), then `npm start` in `heart-quest/` and open http://localhost:4200. |

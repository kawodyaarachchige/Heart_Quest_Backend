package uob.cis045.heartquest.main;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import uob.cis045.heartquest.ApiError;
import uob.cis045.heartquest.client.HeartApiClient;
import uob.cis045.heartquest.client.PuzzleProvider;
import uob.cis045.heartquest.controller.AuthController;
import uob.cis045.heartquest.controller.GameController;
import uob.cis045.heartquest.controller.LeaderboardController;
import uob.cis045.heartquest.exception.InvalidCredentialsException;
import uob.cis045.heartquest.exception.PuzzleFetchException;
import uob.cis045.heartquest.exception.RepositoryException;
import uob.cis045.heartquest.exception.UserAlreadyExistsException;
import uob.cis045.heartquest.repository.ScoreRepository;
import uob.cis045.heartquest.repository.UserRepository;
import uob.cis045.heartquest.repository.sqlite.SqliteMigration;
import uob.cis045.heartquest.repository.sqlite.SqliteScoreRepository;
import uob.cis045.heartquest.repository.sqlite.SqliteUserRepository;
import uob.cis045.heartquest.service.AuthService;
import uob.cis045.heartquest.service.CredentialsValidator;
import uob.cis045.heartquest.service.GameService;
import uob.cis045.heartquest.service.ScoreCalculator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Application bootstrap class responsible for wiring dependencies
 * and registering HTTP routes.
 */
public final class Application {

  private Application() {
    // no-op
  }

  public static void main(String[] args) {
    var port = Integer.parseInt(System.getenv().getOrDefault("HQ_PORT", "7070"));
    var dbPath = Path.of(System.getenv().getOrDefault("HQ_DB_PATH", defaultDbPath().toString()));

    var jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();

    // Infrastructure wiring: migration + repositories.
    var migration = new SqliteMigration(dbPath, jdbcUrl);
    migration.migrate();

    UserRepository userRepository = new SqliteUserRepository(jdbcUrl);
    ScoreRepository scoreRepository = new SqliteScoreRepository(jdbcUrl);

    // Application services.
    var credentialsValidator = new CredentialsValidator();
    var scoreCalculator = new ScoreCalculator();
    var authService = new AuthService(userRepository, credentialsValidator);
    PuzzleProvider puzzleProvider = new HeartApiClient();
    var gameService = new GameService(scoreRepository, puzzleProvider, scoreCalculator);

    var app = Javalin.create(config -> {
      // CORS for local Angular dev (only needed if not using proxy)
      config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
        rule.allowCredentials = true;
        rule.allowHost("http://localhost:4200");
        rule.allowHost("http://localhost:4201");
        rule.allowHost("http://127.0.0.1:4200");
        rule.allowHost("http://127.0.0.1:4201");
      }));
    });

    // Centralized error handling: map domain and infrastructure exceptions to HTTP responses.
    app.exception(ApiError.class, (e, ctx) ->
      ctx.status(HttpStatus.forStatus(e.status())).json(Map.of("error", e.getMessage()))
    );
    app.exception(UserAlreadyExistsException.class, (e, ctx) ->
      ctx.status(HttpStatus.CONFLICT).json(Map.of("error", e.getMessage()))
    );
    app.exception(InvalidCredentialsException.class, (e, ctx) ->
      ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()))
    );
    app.exception(PuzzleFetchException.class, (e, ctx) ->
      ctx.status(HttpStatus.BAD_GATEWAY).json(Map.of("error", e.getMessage()))
    );
    app.exception(RepositoryException.class, (e, ctx) ->
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", "Internal data access error"))
    );

    // Health
    app.get("/api/health", ctx -> ctx.json(Map.of("ok", true)));

    // Feature controllers
    var authController = new AuthController(authService, gameService, scoreRepository);
    var gameController = new GameController(gameService);
    var leaderboardController = new LeaderboardController(scoreRepository);

    authController.registerRoutes(app);
    gameController.registerRoutes(app);
    leaderboardController.registerRoutes(app);

    app.start(port);
    System.out.println("Heart Quest backend running on http://localhost:" + port);
  }

  private static Path defaultDbPath() {
    // Prefer a single stable DB location even if the backend is started from different working directories.
    // Override with HQ_DB_PATH if you want a custom location.
    var cwd = Path.of("").toAbsolutePath();
    if ("heart-quest-backend".equals(cwd.getFileName() == null ? null : cwd.getFileName().toString())) {
      return cwd.resolve("data/heartquest.db");
    }
    var backendDir = cwd.resolve("heart-quest-backend");
    if (Files.isDirectory(backendDir)) {
      return backendDir.resolve("data/heartquest.db");
    }
    return cwd.resolve("data/heartquest.db");
  }
}


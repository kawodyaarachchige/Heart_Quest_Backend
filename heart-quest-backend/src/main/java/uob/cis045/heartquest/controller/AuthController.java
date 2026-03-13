package uob.cis045.heartquest.controller;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import uob.cis045.heartquest.util.Json;
import uob.cis045.heartquest.dto.auth.ForgotPasswordRequest;
import uob.cis045.heartquest.dto.auth.LoginRequest;
import uob.cis045.heartquest.dto.auth.RegisterRequest;
import uob.cis045.heartquest.dto.auth.ResetPasswordRequest;
import uob.cis045.heartquest.domain.GameSession;
import uob.cis045.heartquest.repository.ScoreRepository;
import uob.cis045.heartquest.service.AuthService;
import uob.cis045.heartquest.service.GameService;

import java.util.Map;

/**
 * Controller responsible for authentication-related HTTP endpoints.
 */
public final class AuthController {

  private final AuthService authService;
  private final GameService gameService;
  private final ScoreRepository scoreRepository;

  public AuthController(AuthService authService, GameService gameService, ScoreRepository scoreRepository) {
    this.authService = authService;
    this.gameService = gameService;
    this.scoreRepository = scoreRepository;
  }

  public void registerRoutes(Javalin app) {
    app.post("/api/auth/register", ctx -> {
      var body = Json.read(ctx, RegisterRequest.class);
      authService.register(body.username(), body.password());
      ctx.status(HttpStatus.CREATED).json(Map.of("ok", true));
    });

    app.post("/api/auth/login", ctx -> {
      var body = Json.read(ctx, LoginRequest.class);
      var user = authService.login(body.username(), body.password());

      var session = ctx.req().getSession(true);
      session.setAttribute("userId", user.id());
      session.setAttribute("username", user.username());
      GameSessionStore.createNew(session, user.id(), user.username(), "timed");

      ctx.json(Map.of("id", user.id(), "username", user.username()));
    });

    app.post("/api/auth/logout", ctx -> {
      var session = ctx.req().getSession(false);
      if (session != null) {
        session.invalidate();
      }
      ctx.json(Map.of("ok", true));
    });

    // Forgot password (virtual identity / authentication demo; no email sent)
    app.post("/api/auth/forgot-password", ctx -> {
      var body = Json.read(ctx, ForgotPasswordRequest.class);
      if (body.username() == null || body.username().isBlank()) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Username required"));
        return;
      }
      ctx.json(Map.of("message", "If an account exists, you will receive instructions."));
    });

    // Reset password (set new password for existing account)
    app.post("/api/auth/reset-password", ctx -> {
      var body = Json.read(ctx, ResetPasswordRequest.class);
      var username = body.username() == null ? "" : body.username().trim();
      var newPassword = body.newPassword() == null ? "" : body.newPassword();
      if (username.isBlank()) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Username required"));
        return;
      }
      if (newPassword.isBlank()) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "New password required"));
        return;
      }
      authService.resetPassword(username, newPassword);
      ctx.json(Map.of("ok", true, "message", "Password has been reset. You can now log in."));
    });

    app.get("/api/auth/me", ctx -> {
      var session = SessionUtils.requireLogin(ctx);
      var userId = (Long) session.getAttribute("userId");
      var username = (String) session.getAttribute("username");
      var bestScore = scoreRepository.getBestScore(userId);
      var totalGamesPlayed = scoreRepository.getTotalGamesPlayed(userId);
      ctx.json(Map.of(
        "id", userId,
        "username", username,
        "bestScore", bestScore,
        "totalGamesPlayed", totalGamesPlayed
      ));
    });
  }
}


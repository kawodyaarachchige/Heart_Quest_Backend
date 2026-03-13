package uob.cis045.heartquest.controller;

import io.javalin.Javalin;
import uob.cis045.heartquest.util.Json;
import uob.cis045.heartquest.domain.GameSession;
import uob.cis045.heartquest.dto.game.NewGameRequest;
import uob.cis045.heartquest.dto.game.SkipRequest;
import uob.cis045.heartquest.dto.game.SubmitRequest;
import uob.cis045.heartquest.dto.game.TimeoutRequest;
import uob.cis045.heartquest.service.GameService;

/**
 * Controller responsible for game-related HTTP endpoints.
 */
public final class GameController {

  private final GameService gameService;

  public GameController(GameService gameService) {
    this.gameService = gameService;
  }

  public void registerRoutes(Javalin app) {
    app.post("/api/game/new", ctx -> {
      var session = SessionUtils.requireLogin(ctx);
      var body = Json.read(ctx, NewGameRequest.class);
      GameSession gameSession = GameSessionStore.getOrCreate(session);
      gameService.newGame(gameSession, body.mode());
      ctx.json(gameService.getState(gameSession));
    });

    app.get("/api/puzzle", ctx -> {
      var session = SessionUtils.requireLogin(ctx);
      GameSession gameSession = GameSessionStore.getOrCreate(session);
      ctx.json(gameService.createPuzzle(gameSession));
    });

    app.post("/api/round/submit", ctx -> {
      var session = SessionUtils.requireLogin(ctx);
      var body = Json.read(ctx, SubmitRequest.class);
      GameSession gameSession = GameSessionStore.getOrCreate(session);
      ctx.json(gameService.submit(gameSession, body.puzzleId(), body.answer()));
    });

    app.post("/api/round/skip", ctx -> {
      var session = SessionUtils.requireLogin(ctx);
      var body = Json.read(ctx, SkipRequest.class);
      GameSession gameSession = GameSessionStore.getOrCreate(session);
      ctx.json(gameService.skip(gameSession, body.puzzleId()));
    });

    app.post("/api/round/timeout", ctx -> {
      var session = SessionUtils.requireLogin(ctx);
      var body = Json.read(ctx, TimeoutRequest.class);
      GameSession gameSession = GameSessionStore.getOrCreate(session);
      ctx.json(gameService.timeout(gameSession, body.puzzleId()));
    });
  }
}


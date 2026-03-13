package uob.cis045.heartquest.controller;

import jakarta.servlet.http.HttpSession;
import uob.cis045.heartquest.domain.GameSession;

/**
 * Helper that maps between {@link HttpSession} and the domain-level
 * {@link GameSession} aggregate.
 */
public final class GameSessionStore {

  private static final String ATTRIBUTE_KEY = "gameSession";

  private GameSessionStore() {
  }

  /**
   * Creates a fresh {@link GameSession} for the given authenticated user and
   * stores it in the HTTP session.
   */
  public static GameSession createNew(HttpSession httpSession, long userId, String username, String initialMode) {
    var session = new GameSession(userId, username, initialMode);
    session.setScore(0);
    session.setStreak(0);
    session.setRound(0);
    httpSession.setAttribute(ATTRIBUTE_KEY, session);
    return session;
  }

  /**
   * Returns the current {@link GameSession} or creates one with a default mode
   * if none exists yet.
   */
  public static GameSession getOrCreate(HttpSession httpSession) {
    var existing = httpSession.getAttribute(ATTRIBUTE_KEY);
    if (existing instanceof GameSession gameSession) {
      return gameSession;
    }
    var userId = (Long) httpSession.getAttribute("userId");
    var username = (String) httpSession.getAttribute("username");
    if (userId == null || username == null) {
      throw new IllegalStateException("Cannot create GameSession without authenticated user");
    }
    return createNew(httpSession, userId, username, "timed");
  }
}


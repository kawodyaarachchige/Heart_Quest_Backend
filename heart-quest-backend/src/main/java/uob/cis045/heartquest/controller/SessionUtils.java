package uob.cis045.heartquest.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import uob.cis045.heartquest.ApiError;

/**
 * Small helper for common session-related operations used by controllers.
 * This keeps authentication concerns out of the main application bootstrap.
 */
public final class SessionUtils {

  private SessionUtils() {
    // utility class
  }

  /**
   * Ensures that a user is logged in for the given request context.
   *
   * @param ctx the Javalin request context
   * @return the active HTTP session (never {@code null})
   * @throws ApiError UNAUTHORIZED if no user is logged in
   */
  public static HttpSession requireLogin(Context ctx) {
    var session = ctx.req().getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
      throw new ApiError(HttpStatus.UNAUTHORIZED, "Not logged in");
    }
    return session;
  }
}


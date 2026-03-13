package uob.cis045.heartquest;

import io.javalin.http.HttpStatus;

public final class ApiError extends RuntimeException {
  private final int status;

  public ApiError(HttpStatus status, String message) {
    super(message);
    this.status = status.getCode();
  }

  public int status() {
    return status;
  }
}


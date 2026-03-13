package uob.cis045.heartquest.exception;

/**
 * Thrown when provided credentials are syntactically or semantically invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException(String message) {
    super(message);
  }
}


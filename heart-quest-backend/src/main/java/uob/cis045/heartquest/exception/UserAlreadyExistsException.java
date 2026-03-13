package uob.cis045.heartquest.exception;

/**
 * Thrown when attempting to register a username that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {

  public UserAlreadyExistsException(String message) {
    super(message);
  }
}


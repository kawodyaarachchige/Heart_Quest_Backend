package uob.cis045.heartquest.exception;

/**
 * Thrown when a puzzle cannot be fetched from an external provider.
 */
public class PuzzleFetchException extends RuntimeException {

  public PuzzleFetchException(String message) {
    super(message);
  }

  public PuzzleFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}


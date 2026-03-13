package uob.cis045.heartquest.exception;

/**
 * Unchecked exception indicating a low-level persistence failure.
 * <p>
 * These are intended to be translated at the HTTP boundary into
 * appropriate error responses rather than thrown directly to clients.
 * </p>
 */
public class RepositoryException extends RuntimeException {

  public RepositoryException(String message) {
    super(message);
  }

  public RepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}


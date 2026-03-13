package uob.cis045.heartquest.dto.auth;

/**
 * Request body for user login.
 */
public record LoginRequest(String username, String password) {
}


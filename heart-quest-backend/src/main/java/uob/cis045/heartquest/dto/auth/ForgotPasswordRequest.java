package uob.cis045.heartquest.dto.auth;

/**
 * Request body for forgot-password (virtual identity / authentication flow).
 * Used for coursework demonstration; no email is sent.
 */
public record ForgotPasswordRequest(String username) {
}

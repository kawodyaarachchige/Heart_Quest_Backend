package uob.cis045.heartquest.dto.auth;

/**
 * Request body for resetting password (forgot-password flow).
 */
public record ResetPasswordRequest(String username, String newPassword) {
}

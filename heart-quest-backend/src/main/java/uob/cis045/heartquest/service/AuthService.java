package uob.cis045.heartquest.service;

import io.javalin.http.HttpStatus;
import uob.cis045.heartquest.ApiError;
import uob.cis045.heartquest.exception.UserAlreadyExistsException;
import uob.cis045.heartquest.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Application service responsible for user registration and authentication.
 */
public final class AuthService {

  private final UserRepository userRepository;
  private final CredentialsValidator credentialsValidator;

  public AuthService(UserRepository userRepository, CredentialsValidator credentialsValidator) {
    this.userRepository = userRepository;
    this.credentialsValidator = credentialsValidator;
  }

  public void register(String usernameRaw, String passwordRaw) {
    var username = credentialsValidator.normalizeUsername(usernameRaw);
    var password = credentialsValidator.normalizePassword(passwordRaw);
    var hash = hashPassword(password);

    if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
      throw new UserAlreadyExistsException("Username already exists");
    }

    userRepository.insertUser(username, hash);
  }

  public record AuthUser(long id, String username) {}

  public AuthUser login(String usernameRaw, String passwordRaw) {
    var username = credentialsValidator.normalizeUsername(usernameRaw);
    var password = credentialsValidator.normalizePassword(passwordRaw);

    var user = userRepository.findByUsernameIgnoreCase(username)
      .orElseThrow(() -> new ApiError(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

    var hash = hashPassword(password);
    if (!hash.equals(user.passwordHash())) {
      throw new ApiError(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    return new AuthUser(user.id(), user.username());
  }

  /**
   * Resets password for the given username (e.g. after "forgot password").
   * Lookup is case-insensitive; update uses the stored username from the DB.
   */
  public void resetPassword(String usernameRaw, String newPasswordRaw) {
    var username = credentialsValidator.normalizeUsername(usernameRaw);
    var newPassword = credentialsValidator.normalizePassword(newPasswordRaw);
    var user = userRepository.findByUsernameIgnoreCase(username)
      .orElseThrow(() -> new ApiError(HttpStatus.NOT_FOUND, "Account not found"));
    var hash = hashPassword(newPassword);
    userRepository.updatePassword(user.username(), hash);
  }

  private static String hashPassword(String password) {
    // Hashing the password using SHA-256.
    try {
      var md = MessageDigest.getInstance("SHA-256");
      var bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}


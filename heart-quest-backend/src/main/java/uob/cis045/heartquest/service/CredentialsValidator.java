package uob.cis045.heartquest.service;

import uob.cis045.heartquest.exception.InvalidCredentialsException;

/**
 * Encapsulates username and password validation rules.
 * <p>
 * Keeping this logic in a dedicated component improves cohesion in
 * {@code AuthService} and makes the rules easier to reuse and test.
 * </p>
 */
public final class CredentialsValidator {

  public String normalizeUsername(String raw) {
    var u = raw == null ? "" : raw.trim();
    if (u.length() < 3) {
      throw new InvalidCredentialsException("Username must be at least 3 characters");
    }
    if (u.length() > 32) {
      throw new InvalidCredentialsException("Username too long");
    }
    if (!u.matches("[A-Za-z0-9_]+")) {
      throw new InvalidCredentialsException("Username must be alphanumeric/underscore");
    }
    return u;
  }

  public String normalizePassword(String raw) {
    var p = raw == null ? "" : raw;
    if (p.length() < 4) {
      throw new InvalidCredentialsException("Password must be at least 4 characters");
    }
    if (p.length() > 200) {
      throw new InvalidCredentialsException("Password too long");
    }
    return p;
  }
}


package uob.cis045.heartquest.domain;

/**
 * Core domain representation of an authenticated user.
 */
public record User(long id, String username, String passwordHash) {
}


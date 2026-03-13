package uob.cis045.heartquest.domain;

import java.time.Instant;

/**
 * Best score achieved by a user.
 */
public record Score(long userId, int bestScore, Instant updatedAt) {
}


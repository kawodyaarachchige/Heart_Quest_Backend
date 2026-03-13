package uob.cis045.heartquest.domain;

/**
 * Entry in the public leaderboard, exposing only user-facing data.
 */
public record LeaderboardEntry(String username, int bestScore, String updatedAtIso) {
}


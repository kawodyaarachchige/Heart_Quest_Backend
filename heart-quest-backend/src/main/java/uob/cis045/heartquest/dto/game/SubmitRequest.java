package uob.cis045.heartquest.dto.game;

/**
 * Request body for submitting an answer in a round.
 */
public record SubmitRequest(String puzzleId, int answer) {
}


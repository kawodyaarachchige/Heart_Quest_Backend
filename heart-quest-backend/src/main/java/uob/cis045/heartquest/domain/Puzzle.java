package uob.cis045.heartquest.domain;

/**
 * Logic-level representation of a puzzle delivered to the user.
 */
public record Puzzle(String imageDataUrl, int solution, int carrots) {
}


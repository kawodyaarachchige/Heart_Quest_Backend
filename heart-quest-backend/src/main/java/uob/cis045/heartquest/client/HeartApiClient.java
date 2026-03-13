package uob.cis045.heartquest.client;

import com.fasterxml.jackson.databind.JsonNode;
import uob.cis045.heartquest.domain.Puzzle;
import uob.cis045.heartquest.exception.PuzzleFetchException;
import uob.cis045.heartquest.util.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client that fetches puzzles from the external Heart API.
 */
public final class HeartApiClient implements PuzzleProvider {

  private final HttpClient http = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build();

  @Override
  public Puzzle fetchPuzzle() {
    try {
      // marcconrad.com redirects http -> https (301)
      var uri = URI.create("https://marcconrad.com/uob/heart/api.php?out=json&base64=yes");
      var req = HttpRequest.newBuilder(uri)
        .timeout(Duration.ofSeconds(20))
        .GET()
        .build();

      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() != 200) {
        throw new PuzzleFetchException("Heart API returned " + resp.statusCode());
      }

      JsonNode n = Json.mapper().readTree(resp.body());
      var base64 = n.get("question").asText();
      var solution = n.get("solution").asInt();
      var carrots = n.get("carrots").asInt();

      return new Puzzle("data:image/png;base64," + base64, solution, carrots);
    } catch (PuzzleFetchException e) {
      throw e;
    } catch (Exception e) {
      throw new PuzzleFetchException("Failed to fetch Heart API puzzle", e);
    }
  }
}


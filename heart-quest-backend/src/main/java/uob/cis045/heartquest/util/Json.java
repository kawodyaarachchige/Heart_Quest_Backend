package uob.cis045.heartquest.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import uob.cis045.heartquest.ApiError;

import java.io.IOException;

/**
 * Central JSON helper used by controllers and HTTP clients.
 */
public final class Json {

  private static final ObjectMapper MAPPER = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private Json() {
  }

  public static <T> T read(Context ctx, Class<T> clazz) {
    try {
      return MAPPER.readValue(ctx.body(), clazz);
    } catch (IOException e) {
      throw new ApiError(HttpStatus.BAD_REQUEST, "Invalid JSON body");
    }
  }

  public static ObjectMapper mapper() {
    return MAPPER;
  }
}


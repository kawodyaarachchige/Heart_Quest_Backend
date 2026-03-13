package uob.cis045.heartquest.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uob.cis045.heartquest.exception.InvalidCredentialsException;

class CredentialsValidatorTest {

  private final CredentialsValidator validator = new CredentialsValidator();

  @Test
  void normalizeUsername_trimsAndAcceptsValid() {
    Assertions.assertEquals("user_1", validator.normalizeUsername("  user_1  "));
  }

  @Test
  void normalizeUsername_rejectsTooShort() {
    Assertions.assertThrows(InvalidCredentialsException.class, () -> validator.normalizeUsername("ab"));
  }

  @Test
  void normalizePassword_rejectsTooShort() {
    Assertions.assertThrows(InvalidCredentialsException.class, () -> validator.normalizePassword("123"));
  }
}


package metric.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String SECRET =
      "mySecretKeyForMetricProjectMustBeAtLeast32BytesLong";

  private JwtTokenProvider provider;

  @BeforeEach
  void setUp() {
    provider = new JwtTokenProvider(SECRET, 86_400_000L);
  }

  @Test
  void createTokenShouldContainExpectedClaims() {
    String token = provider.createToken("alice", "admin", 10L);

    assertNotNull(token);
    assertFalse(token.isBlank());
    assertTrue(provider.validateToken(token));
    assertEquals("alice", provider.getUsername(token));
  }

  @Test
  void validateTokenShouldReturnTrueForValidToken() {
    String token = provider.createToken("alice", "user", 1L);

    assertTrue(provider.validateToken(token));
  }

  @Test
  void getUsernameShouldReturnSubject() {
    String token = provider.createToken("bob", "admin", 2L);

    assertEquals("bob", provider.getUsername(token));
  }

  @Test
  void validateTokenShouldReturnFalseForMalformedToken() {
    assertFalse(provider.validateToken("not-a-jwt"));
  }

  @Test
  void validateTokenShouldReturnFalseForEmptyToken() {
    assertFalse(provider.validateToken(""));
  }

  @Test
  void validateTokenShouldReturnFalseForNullToken() {
    assertFalse(provider.validateToken(null));
  }

  @Test
  void validateTokenShouldReturnFalseForExpiredToken() {
    SecretKey key = Keys.hmacShaKeyFor(
        SECRET.getBytes(StandardCharsets.UTF_8));

    Date now = new Date();
    String expiredToken = Jwts.builder()
        .subject("alice")
        .issuedAt(new Date(now.getTime() - 10_000))
        .expiration(new Date(now.getTime() - 1_000))
        .signWith(key)
        .compact();

    assertFalse(provider.validateToken(expiredToken));
  }

  @Test
  void validateTokenShouldReturnFalseForTokenSignedWithDifferentKey() {
    JwtTokenProvider anotherProvider = new JwtTokenProvider(
        "anotherSecretKeyForMetricProjectMustBeAtLeast32Bytes",
        86_400_000L);

    String token = anotherProvider.createToken("alice", "user", 1L);

    assertFalse(provider.validateToken(token));
  }

  @Test
  void getUsernameShouldRejectInvalidToken() {
    assertThrows(
        Exception.class,
        () -> provider.getUsername("invalid-token"));
  }

  @Test
  void constructorShouldRejectShortSecret() {
    assertThrows(
        WeakKeyException.class,
        () -> new JwtTokenProvider("short-secret", 60_000L));
  }

  @Test
  void createTokenShouldRespectExpirationConfiguration() {
    JwtTokenProvider shortLivedProvider =
        new JwtTokenProvider(SECRET, -1_000L);

    String token = shortLivedProvider.createToken("alice", "user", 1L);

    assertFalse(shortLivedProvider.validateToken(token));
  }
}

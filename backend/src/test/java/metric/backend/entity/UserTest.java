package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void shouldInitializeBuilderDefaultRole() {
    User user = User.builder().build();

    assertEquals(Role.user, user.getRole());
    assertNull(user.getUserId());
    assertNull(user.getFullName());
    assertNull(user.getUsername());
    assertNull(user.getEmail());
    assertNull(user.getPasswordHash());
    assertNull(user.getPhone());
    assertNull(user.getCountryCode());
    assertNull(user.getDateOfBirth());
    assertNull(user.getAddress());
    assertNull(user.getProfileImage());
    assertNull(user.getCreatedAt());
  }

  @Test
  void shouldSupportBuilderAndAllFields() {
    LocalDate birthDate = LocalDate.of(2000, 1, 15);
    OffsetDateTime createdAt = OffsetDateTime.now();

    User user = User.builder()
        .userId(1L)
        .fullName("Alice Smith")
        .username("alice")
        .email("alice@example.com")
        .passwordHash("hashed-password")
        .phone("0812345678")
        .countryCode("+66")
        .dateOfBirth(birthDate)
        .address("Chiang Mai, Thailand")
        .profileImage("/avatars/alice.png")
        .role(Role.admin)
        .createdAt(createdAt)
        .build();

    assertEquals(1L, user.getUserId());
    assertEquals("Alice Smith", user.getFullName());
    assertEquals("alice", user.getUsername());
    assertEquals("alice@example.com", user.getEmail());
    assertEquals("hashed-password", user.getPasswordHash());
    assertEquals("0812345678", user.getPhone());
    assertEquals("+66", user.getCountryCode());
    assertEquals(birthDate, user.getDateOfBirth());
    assertEquals("Chiang Mai, Thailand", user.getAddress());
    assertEquals("/avatars/alice.png", user.getProfileImage());
    assertEquals(Role.admin, user.getRole());
    assertEquals(createdAt, user.getCreatedAt());
  }

  @Test
  void shouldSupportAllSetters() {
    User user = new User();

    LocalDate birthDate = LocalDate.of(1999, 12, 31);
    OffsetDateTime createdAt = OffsetDateTime.now();

    user.setUserId(2L);
    user.setFullName("Bob Smith");
    user.setUsername("bob");
    user.setEmail("bob@example.com");
    user.setPasswordHash("another-hash");
    user.setPhone("0899999999");
    user.setCountryCode("+66");
    user.setDateOfBirth(birthDate);
    user.setAddress("Bangkok");
    user.setProfileImage("/avatars/bob.png");
    user.setRole(Role.admin);
    user.setCreatedAt(createdAt);

    assertEquals(2L, user.getUserId());
    assertEquals("Bob Smith", user.getFullName());
    assertEquals("bob", user.getUsername());
    assertEquals("bob@example.com", user.getEmail());
    assertEquals("another-hash", user.getPasswordHash());
    assertEquals("0899999999", user.getPhone());
    assertEquals("+66", user.getCountryCode());
    assertEquals(birthDate, user.getDateOfBirth());
    assertEquals("Bangkok", user.getAddress());
    assertEquals("/avatars/bob.png", user.getProfileImage());
    assertEquals(Role.admin, user.getRole());
    assertEquals(createdAt, user.getCreatedAt());
  }

  @Test
  void shouldSupportAllArgsConstructor() {
    LocalDate birthDate = LocalDate.of(2001, 5, 20);
    OffsetDateTime createdAt = OffsetDateTime.now();

    User user = new User(
        1L,
        "Alice",
        "alice",
        "alice@example.com",
        "hashed",
        "0800000000",
        "+66",
        birthDate,
        "Thailand",
        "/avatar.png",
        Role.user,
        createdAt);

    assertEquals(1L, user.getUserId());
    assertEquals("Alice", user.getFullName());
    assertEquals("alice", user.getUsername());
    assertEquals("alice@example.com", user.getEmail());
    assertEquals("hashed", user.getPasswordHash());
    assertEquals("0800000000", user.getPhone());
    assertEquals("+66", user.getCountryCode());
    assertEquals(birthDate, user.getDateOfBirth());
    assertEquals("Thailand", user.getAddress());
    assertEquals("/avatar.png", user.getProfileImage());
    assertEquals(Role.user, user.getRole());
    assertEquals(createdAt, user.getCreatedAt());
  }

  @Test
  void shouldAllowOptionalFieldsToBeNull() {
    User user = User.builder()
        .userId(1L)
        .fullName("Alice")
        .username("alice")
        .email("alice@example.com")
        .passwordHash("hashed")
        .phone(null)
        .countryCode(null)
        .dateOfBirth(null)
        .address(null)
        .profileImage(null)
        .createdAt(null)
        .build();

    assertNull(user.getPhone());
    assertNull(user.getCountryCode());
    assertNull(user.getDateOfBirth());
    assertNull(user.getAddress());
    assertNull(user.getProfileImage());
    assertNull(user.getCreatedAt());
  }
}
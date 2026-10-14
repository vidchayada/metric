package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void shouldContainAllExpectedRoles() {
    assertArrayEquals(
        new Role[]{Role.USER, Role.ADMIN},
        Role.values());
  }

  @Test
  void shouldConvertNamesToEnumValues() {
    assertEquals(Role.USER, Role.valueOf("USER"));
    assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
  }

  @Test
  void shouldReturnCorrectOrdinals() {
    assertEquals(0, Role.USER.ordinal());
    assertEquals(1, Role.ADMIN.ordinal());
  }

  @Test
  void shouldRejectUnknownRole() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Role.valueOf("UNKNOWN"));
  }
}

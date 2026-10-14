package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void shouldContainAllExpectedRoles() {
    assertArrayEquals(
        new Role[]{Role.user, Role.admin},
        Role.values());
  }

  @Test
  void shouldConvertNamesToEnumValues() {
    assertEquals(Role.user, Role.valueOf("user"));
    assertEquals(Role.admin, Role.valueOf("admin"));
  }

  @Test
  void shouldReturnCorrectOrdinals() {
    assertEquals(0, Role.user.ordinal());
    assertEquals(1, Role.admin.ordinal());
  }

  @Test
  void shouldRejectUnknownRole() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Role.valueOf("UNKNOWN"));
  }
}

package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpdateProfileRequestTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    var request = UpdateProfileRequest.builder()
        .fullName("Alice Smith")
        .dateOfBirth("2000-01-01")
        .phone("0812345678")
        .address("Chiang Mai")
        .avatarUrl("/avatars/alice.png")
        .build();

    assertEquals("Alice Smith", request.getFullName());
    assertEquals("2000-01-01", request.getDateOfBirth());
    assertEquals("0812345678", request.getPhone());
    assertEquals("Chiang Mai", request.getAddress());
    assertEquals("/avatars/alice.png", request.getAvatarUrl());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new UpdateProfileRequest().getFullName());

    var request = new UpdateProfileRequest(
        "Alice", "2000-01-01", "0800000000",
        "Bangkok", "/avatar.png");

    request.setFullName("Bob");
    request.setDateOfBirth("1999-12-31");
    request.setPhone("0899999999");
    request.setAddress("Phuket");
    request.setAvatarUrl("/new-avatar.png");

    assertEquals("Bob", request.getFullName());
    assertEquals("1999-12-31", request.getDateOfBirth());
    assertEquals("0899999999", request.getPhone());
    assertEquals("Phuket", request.getAddress());
    assertEquals("/new-avatar.png", request.getAvatarUrl());
  }

  @Test
  void shouldAllowNullOptionalFields() {
    var request = UpdateProfileRequest.builder().build();

    assertNull(request.getFullName());
    assertNull(request.getDateOfBirth());
    assertNull(request.getPhone());
    assertNull(request.getAddress());
    assertNull(request.getAvatarUrl());
  }
}

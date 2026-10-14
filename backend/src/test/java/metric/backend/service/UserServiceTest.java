package metric.backend.service;

import metric.backend.dto.UpdateProfileRequest;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User user;

  private static final Long USER_ID = 1L;
  private static final String DEFAULT_AVATAR =
      "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400";

  @BeforeEach
  void setUp() {
    user = new User();
    user.setUserId(USER_ID);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setFullName("Test User");
    user.setPhone("0812345678");
    user.setAddress("Bangkok");
    user.setDateOfBirth(LocalDate.of(2000, 1, 15));
    user.setProfileImage(null);
  }

  @Test
  void getCurrentUserProfile_shouldReturnUserProfile() {
    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    Map<String, Object> result =
        userService.getCurrentUserProfile(USER_ID);

    assertEquals(USER_ID, result.get("id"));
    assertEquals(USER_ID, result.get("userId"));
    assertEquals("testuser", result.get("username"));
    assertEquals("test@example.com", result.get("email"));
    assertEquals("Test User", result.get("fullName"));
    assertEquals("0812345678", result.get("phone"));
    assertEquals("Bangkok", result.get("address"));
    assertEquals("2000-01-15", result.get("dateOfBirth"));
    assertEquals("USER", result.get("role"));
    assertEquals(DEFAULT_AVATAR, result.get("avatar"));
    assertEquals(DEFAULT_AVATAR, result.get("avatarUrl"));
    assertEquals(DEFAULT_AVATAR, result.get("profileImage"));

    verify(userRepository).findById(USER_ID);
  }

  @Test
  void getCurrentUserProfile_shouldReturnNullBirthDate() {
    user.setDateOfBirth(null);

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    Map<String, Object> result =
        userService.getCurrentUserProfile(USER_ID);

    assertNull(result.get("dateOfBirth"));
  }

  @Test
  void getCurrentUserProfile_shouldReturnExistingAvatar() {
    user.setProfileImage("data:image/png;base64,abc123");

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    Map<String, Object> result =
        userService.getCurrentUserProfile(USER_ID);

    assertEquals(
        "data:image/png;base64,abc123",
        result.get("avatar")
    );
    assertEquals(result.get("avatar"), result.get("avatarUrl"));
    assertEquals(result.get("avatar"), result.get("profileImage"));
  }

  @Test
  void getCurrentUserProfile_shouldUseDefaultAvatarWhenBlank() {
    user.setProfileImage("   ");

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    Map<String, Object> result =
        userService.getCurrentUserProfile(USER_ID);

    assertEquals(DEFAULT_AVATAR, result.get("avatar"));
  }

  @Test
  void getCurrentUserProfile_shouldThrowWhenUserNotFound() {
    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> userService.getCurrentUserProfile(USER_ID)
    );

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void updateProfile_shouldUpdateAllFields() {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setFullName("Updated Name");
    request.setPhone("0899999999");
    request.setAddress("Chiang Mai");
    request.setDateOfBirth("1999-05-20");

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    Map<String, Object> result =
        userService.updateProfile(USER_ID, request);

    assertEquals("Updated Name", user.getFullName());
    assertEquals("0899999999", user.getPhone());
    assertEquals("Chiang Mai", user.getAddress());
    assertEquals(
        LocalDate.of(1999, 5, 20),
        user.getDateOfBirth()
    );

    assertEquals("Updated Name", result.get("fullName"));
    assertEquals("1999-05-20", result.get("dateOfBirth"));

    verify(userRepository).save(user);
    verify(userRepository, times(2)).findById(USER_ID);
  }

  @Test
  void updateProfile_shouldNotChangeFieldsWhenValuesAreNull() {
    UpdateProfileRequest request = new UpdateProfileRequest();

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    Map<String, Object> result =
        userService.updateProfile(USER_ID, request);

    assertEquals("Test User", user.getFullName());
    assertEquals("0812345678", user.getPhone());
    assertEquals("Bangkok", user.getAddress());
    assertEquals(
        LocalDate.of(2000, 1, 15),
        user.getDateOfBirth()
    );

    assertEquals("Test User", result.get("fullName"));
  }

  @Test
  void updateProfile_shouldNotChangeBirthDateWhenEmpty() {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setDateOfBirth("");

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    userService.updateProfile(USER_ID, request);

    assertEquals(
        LocalDate.of(2000, 1, 15),
        user.getDateOfBirth()
    );
  }

  @Test
  void updateProfile_shouldUpdateOnlySpecifiedFields() {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setFullName("New Name");
    request.setPhone(null);
    request.setAddress(null);
    request.setDateOfBirth(null);

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    userService.updateProfile(USER_ID, request);

    assertEquals("New Name", user.getFullName());
    assertEquals("0812345678", user.getPhone());
    assertEquals("Bangkok", user.getAddress());
  }

  @Test
  void updateProfile_shouldThrowWhenUserNotFound() {
    UpdateProfileRequest request = new UpdateProfileRequest();

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.updateProfile(USER_ID, request)
    );

    assertEquals("User not found", exception.getMessage());

    verify(userRepository, never()).save(any());
  }

  @Test
  void updateProfile_shouldThrowWhenBirthDateIsInvalid() {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setDateOfBirth("invalid-date");

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    assertThrows(
        java.time.format.DateTimeParseException.class,
        () -> userService.updateProfile(USER_ID, request)
    );

    verify(userRepository, never()).save(any());
  }

  @Test
  void uploadAvatar_shouldUploadFileSuccessfully() throws Exception {
    byte[] content = "fake-image-content".getBytes();

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "avatar.png",
        "image/png",
        content
    );

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    Map<String, Object> result =
        userService.uploadAvatar(USER_ID, file);

    String expected = "data:image/png;base64,"
        + Base64.getEncoder().encodeToString(content);

    assertEquals(expected, user.getProfileImage());
    assertEquals(expected, result.get("avatar"));
    assertEquals(expected, result.get("avatarUrl"));
    assertEquals(expected, result.get("profileImage"));

    verify(userRepository).save(user);
  }

  @Test
  void uploadAvatar_shouldUseDefaultContentTypeWhenNull()
      throws Exception {
    byte[] content = "image-data".getBytes();

    MultipartFile file = mock(MultipartFile.class);

    when(file.isEmpty()).thenReturn(false);
    when(file.getContentType()).thenReturn(null);
    when(file.getBytes()).thenReturn(content);
    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    userService.uploadAvatar(USER_ID, file);

    String expected = "data:image/jpeg;base64,"
        + Base64.getEncoder().encodeToString(content);

    assertEquals(expected, user.getProfileImage());
  }

  @Test
  void uploadAvatar_shouldThrowWhenFileIsNull() {
    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.uploadAvatar(USER_ID, null)
    );

    assertEquals("File cannot be empty", exception.getMessage());

    verify(userRepository, never()).save(any());
  }

  @Test
  void uploadAvatar_shouldThrowWhenFileIsEmpty() {
    MultipartFile file = mock(MultipartFile.class);

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(file.isEmpty()).thenReturn(true);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.uploadAvatar(USER_ID, file)
    );

    assertEquals("File cannot be empty", exception.getMessage());

    verify(userRepository, never()).save(any());
  }

  @Test
  void uploadAvatar_shouldThrowWhenUserNotFound() {
    MultipartFile file = new MockMultipartFile(
        "file",
        "avatar.png",
        "image/png",
        "content".getBytes()
    );

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> userService.uploadAvatar(USER_ID, file)
    );

    assertEquals("User not found", exception.getMessage());
  }

  @Test
  void uploadAvatar_shouldWrapIOException() throws Exception {
    MultipartFile file = mock(MultipartFile.class);

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));
    when(file.isEmpty()).thenReturn(false);
    when(file.getContentType()).thenReturn("image/png");
    when(file.getBytes())
        .thenThrow(new IOException("Read failed"));

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> userService.uploadAvatar(USER_ID, file)
    );

    assertEquals("Failed to upload avatar", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());

    verify(userRepository, never()).save(any());
  }


  @Test
  void getCurrentUserProfile_shouldReturnRoleNameWhenRoleExists()
      throws Exception {
    setFirstEnumConstantOnRoleField(user);

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user));

    Map<String, Object> result =
        userService.getCurrentUserProfile(USER_ID);

    assertNotNull(result.get("role"));
    assertNotEquals("USER", result.get("role"));
  }

  private void setFirstEnumConstantOnRoleField(User target)
      throws Exception {
    Field roleField = null;
    Class<?> current = target.getClass();

    while (current != null && roleField == null) {
      try {
        roleField = current.getDeclaredField("role");
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }

    assertNotNull(roleField, "User must contain a role field");
    roleField.setAccessible(true);

    Class<?> roleType = roleField.getType();
    assertTrue(roleType.isEnum(), "Role field must be an enum");

    Object[] constants = roleType.getEnumConstants();
    assertNotNull(constants);
    assertTrue(constants.length > 0, "Role enum must have a value");

    roleField.set(target, constants[0]);
  }
}

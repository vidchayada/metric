package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import metric.backend.dto.UpdateProfileRequest;
import metric.backend.entity.User;
import metric.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock UserService userService;

  @InjectMocks UserController controller;

  @Test
  void getCurrentUserShouldReturnUnauthorizedForNullUser() {
    var response = controller.getCurrentUser(null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verifyNoInteractions(userService);
  }

  @Test
  void getCurrentUserShouldReturnProfile() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    Map<String, Object> profile = Map.of("username", "alice");
    when(userService.getCurrentUserProfile(1L)).thenReturn(profile);

    var response = controller.getCurrentUser(user);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(profile, response.getBody());
  }

  @Test
  void updateProfileShouldReturnUnauthorizedForNullUser() {
    var response = controller.updateProfile(null, mock(UpdateProfileRequest.class));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verifyNoInteractions(userService);
  }

  @Test
  void updateProfileShouldDelegateToService() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    UpdateProfileRequest request = mock(UpdateProfileRequest.class);
    Map<String, Object> profile = Map.of("username", "alice");
    when(userService.updateProfile(1L, request)).thenReturn(profile);

    var response = controller.updateProfile(user, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(profile, response.getBody());
  }

  @Test
  void uploadAvatarShouldRejectNullUser() {
    var file = new MockMultipartFile(
        "avatar", "avatar.png", "image/png", new byte[]{1});

    var response = controller.uploadAvatar(null, file, null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verifyNoInteractions(userService);
  }

  @Test
  void uploadAvatarShouldUseAvatarWhenProvided() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var avatar = new MockMultipartFile(
        "avatar", "avatar.png", "image/png", new byte[]{1});
    var fallback = new MockMultipartFile(
        "file", "fallback.png", "image/png", new byte[]{2});

    Map<String, Object> result = Map.of("avatarUrl", "/image/avatar.png");
    when(userService.uploadAvatar(1L, avatar)).thenReturn(result);

    var response = controller.uploadAvatar(user, avatar, fallback);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(result, response.getBody());
    verify(userService).uploadAvatar(1L, avatar);
  }

  @Test
  void uploadAvatarShouldUseFileFallback() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var file = new MockMultipartFile(
        "file", "avatar.png", "image/png", new byte[]{1});
    when(userService.uploadAvatar(eq(1L), eq(file)))
        .thenReturn(Map.of("avatarUrl", "/avatar.png"));

    var response = controller.uploadAvatar(user, null, file);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(userService).uploadAvatar(1L, file);
  }

  @Test
  void uploadAvatarShouldRejectMissingFile() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.uploadAvatar(user, null, null)
    );
  }

  @Test
  void uploadAvatarShouldRejectEmptyFile() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var empty = new MockMultipartFile(
        "avatar", "avatar.png", "image/png", new byte[0]);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.uploadAvatar(user, empty, null)
    );
  }
}

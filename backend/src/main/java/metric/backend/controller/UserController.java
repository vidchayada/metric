package metric.backend.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.UpdateProfileRequest;
import metric.backend.entity.User;
import metric.backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getCurrentUserProfile(user.getUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.updateProfile(user.getUserId(), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MultipartFile targetFile = avatar != null ? avatar : file;
        if (targetFile == null || targetFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        return ResponseEntity.ok(userService.uploadAvatar(user.getUserId(), targetFile));
    }
}
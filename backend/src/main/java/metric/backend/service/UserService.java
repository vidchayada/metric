/*
- Copyright (c) 2026 Vidchayada

- Licensed under the MIT License.

 */

package metric.backend.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.UpdateProfileRequest;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final String USER_NOT_FOUND = "User not found";

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(java.time.LocalDate.parse(request.getDateOfBirth()));
        }

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    @Transactional
    public Map<String, Object> uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String base64Content = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64Content;

            user.setProfileImage(dataUrl);
            userRepository.save(user);

            return mapToUserResponse(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> mapToUserResponse(User user) {
        Map<String, Object> res = new HashMap<>();
        res.put("id", user.getUserId());
        res.put("userId", user.getUserId());
        res.put("username", user.getUsername());
        res.put("email", user.getEmail());
        res.put("fullName", user.getFullName());
        res.put("phone", user.getPhone());
        res.put("address", user.getAddress());
        res.put("dateOfBirth", user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        res.put("role", user.getRole() != null ? user.getRole().name() : "USER");

        String avatar = (user.getProfileImage() != null && !user.getProfileImage().isBlank())
                ? user.getProfileImage()
                : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400";

        res.put("avatar", avatar);
        res.put("avatarUrl", avatar);
        res.put("profileImage", avatar);

        return res;
    }
}
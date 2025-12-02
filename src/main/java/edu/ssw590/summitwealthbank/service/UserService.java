package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.ChangePasswordRequest;
import edu.ssw590.summitwealthbank.dto.UpdatePhoneRequest;
import edu.ssw590.summitwealthbank.dto.UpdateProfilePicRequest;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updatePhone(String email, UpdatePhoneRequest request) {
        User user = getUserByEmail(email);

        // Validate phone number
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        user.setPhone(request.getPhone());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfilePic(String email, UpdateProfilePicRequest request) {
        User user = getUserByEmail(email);

        user.setProfilePicUrl(request.getProfilePicUrl());
        return userRepository.save(user);
    }
}
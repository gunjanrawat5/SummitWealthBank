package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> existing = userRepo.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> found = userRepo.findByUsername(user.getUsername());

        if (found.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username");
        }

        if (!passwordEncoder.matches(user.getPassword(), found.get().getPassword())) {
            return ResponseEntity.status(401).body("Wrong password");
        }

        return ResponseEntity.ok(found.get());
    }
}
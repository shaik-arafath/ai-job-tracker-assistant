package com.aijobtracker.app.auth;

import com.aijobtracker.app.user.User;
import com.aijobtracker.app.user.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public record SignupRequest(@Email String email, @NotBlank String password, @NotBlank String fullName) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, Long userId, String email) {}

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(x -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        });
        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getId(), user.getEmail()), user.getId(), user.getEmail());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthResponse(jwtService.generateToken(user.getId(), user.getEmail()), user.getId(), user.getEmail());
    }
}

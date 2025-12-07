package edu.ssw590.summitwealthbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ssw590.summitwealthbank.dto.LoginRequest;
import edu.ssw590.summitwealthbank.dto.RegisterRequest;
import edu.ssw590.summitwealthbank.model.User;
import edu.ssw590.summitwealthbank.repository.UserRepository;
import edu.ssw590.summitwealthbank.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.initialize-stocks=false"
})
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .role("USER")
                .status("ACTIVE")
                .build();
        userRepository.save(testUser);

        // Create admin user
        adminUser = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .phone("0987654321")
                .role("ADMIN")
                .status("ACTIVE")
                .build();
        userRepository.save(adminUser);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("newpassword123");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhone("5551234567");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Jane"))
                .andExpect(jsonPath("$.user.lastName").value("Smith"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.password").doesNotExist()); // Should not expose password
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void testRegisterDuplicateEmail() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com"); // Already exists
        request.setPassword("password123");
        request.setFirstName("Another");
        request.setLastName("User");
        request.setPhone("5551234567");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"));
    }

    @Test
    @DisplayName("Should fail login with invalid email")
    void testLoginInvalidEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should fail login with invalid password")
    void testLoginInvalidPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should allow admin login with admin credentials")
    void testAdminLoginSuccess() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.email").value("admin@example.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Should deny admin login for non-admin user")
    void testAdminLoginDeniedForRegularUser() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com"); // Regular user, not admin
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied. Admin privileges required."));
    }

    @Test
    @DisplayName("Should fail admin login with invalid credentials")
    void testAdminLoginInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should generate valid JWT token on successful login")
    void testJwtTokenGeneration() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act
        String responseContent = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response
        String token = objectMapper.readTree(responseContent).get("token").asText();

        // Assert - Verify token is valid
        String emailFromToken = jwtUtil.extractEmail(token);
        assert emailFromToken.equals("test@example.com");
        assert jwtUtil.validateToken(token, "test@example.com");
    }

    @Test
    @DisplayName("Should update last login timestamp on successful login")
    void testLastLoginUpdate() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User beforeLogin = userRepository.findByEmail("test@example.com").orElseThrow();
        var lastLoginBefore = beforeLogin.getLastLogin();

        // Wait a bit to ensure timestamp difference
        Thread.sleep(100);

        // Act
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        User afterLogin = userRepository.findByEmail("test@example.com").orElseThrow();
        var lastLoginAfter = afterLogin.getLastLogin();

        assert lastLoginAfter.isAfter(lastLoginBefore);
    }

    @Test
    @DisplayName("Should not expose password in registration response")
    void testPasswordNotExposedInRegisterResponse() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("security@example.com");
        request.setPassword("securepassword123");
        request.setFirstName("Secure");
        request.setLastName("User");
        request.setPhone("5551234567");

        // Act
        String responseContent = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert - Response should not contain password field
        assert !responseContent.contains("securepassword123");
        assert !responseContent.contains("\"password\"");
    }
}
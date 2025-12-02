package edu.ssw590.summitwealthbank.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
    private String phone;
    private String profilePicUrl;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Account> accounts;

    private LocalDateTime createdAt;  // NEW field
    private LocalDateTime lastLogin;  // NEW field

    @PrePersist
    protected void onCreate() {
        if (this.role == null) {
            this.role = "USER";
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        this.createdAt = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();
    }
}
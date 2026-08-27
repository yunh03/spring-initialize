package kr.yuns.springinitialize.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.yuns.springinitialize.user.domain.exception.PasswordInvalidException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime registeredAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime modifiedAt;

    private User(Email email, String name, String password, UserRole role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public static User register(Email email, String name, String rawPassword, PasswordEncoder passwordEncoder) {
        return new User(email, name, passwordEncoder.encode(rawPassword), UserRole.USER);
    }

    public void verifyPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(rawPassword, this.password)) {
            throw new PasswordInvalidException();
        }
    }

    public String getEmailValue() {
        return email.getValue();
    }

    public String getRoleValue() {
        return role.getValue();
    }
}

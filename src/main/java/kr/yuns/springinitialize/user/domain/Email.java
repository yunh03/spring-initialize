package kr.yuns.springinitialize.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import kr.yuns.springinitialize.user.domain.exception.EmailInvalidException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new EmailInvalidException();
        }
        return new Email(value.toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}

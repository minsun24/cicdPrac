package com.lgcns.pipeline.user;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


//@DataJpaTest
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Rollback(false)
class UserRepositoryTest {
    private static final String EMAIL = "admin@email.com";
    private static final String PASSWD = "admin123";
    private static final String NAME = "Hong";

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    void registTest() {
        long orgCount = repository.count();
        User user = User.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWD))
                .name(NAME)
                .build()
                .addRole(UserRole.ROLE_USER)
                .addRole(UserRole.ROLE_ADMIN);

        User newer = repository.save(user);
        assertThat(repository.count()).isGreaterThan(orgCount);
        assertThat(newer.getId()).isNotNull();
    }

    @Test
    @Order(2)
    void getWithRoles() {
        User user = repository.getWithRoles(EMAIL).orElseThrow();
        assertThat(user.getName()).isEqualTo(NAME);
        assertThat(user.getRoles()).containsAll(List.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
        assertThat(passwordEncoder.matches(PASSWD, user.getPassword())).isTrue();
    }

    @Test()
    @Order(3)
    void deleteUser() {
        long orgCount = repository.count();
        User user = repository.findByEmail(EMAIL).orElseThrow();

        repository.delete(user);
        assertThat(repository.count()).isLessThan(orgCount);
    }
}

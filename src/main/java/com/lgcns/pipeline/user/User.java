package com.lgcns.pipeline.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(columnDefinition = "int unsigned")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(name = "UserRole", joinColumns = @JoinColumn(name = "id"))
    @Column(nullable = false)
    @Builder.Default
    @ToString.Exclude
    private List<UserRole> roles = new ArrayList<>();   // 일대다


    public User addRole(UserRole role) {
        if (roles == null)
            roles = new ArrayList<>();
        roles.add(role);
        return this;
    }

    public void clearRoles() {
        if (roles != null)
            roles.clear();
    }


    public User deleteRole(UserRole userRole) {
        if (roles != null)
            return this;
        roles.remove(userRole);
        return this;
    }

}

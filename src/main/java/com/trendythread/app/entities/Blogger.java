package com.trendythread.app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "blogger")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Blogger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String plainPassword;

    private String about;

    @OneToMany(mappedBy = "blogger", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    // Add role field if not present
    @Column(name = "role", nullable = false)
    private String role = "ROLE_USER"; // Default to user, admin will have "ROLE_ADMIN"

    // Helper method
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }
}

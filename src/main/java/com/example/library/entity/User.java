package com.example.library.entity;

import com.example.library.enam.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;


@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE book SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // Это замена старой @Where
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false) // Новое поле
    private String firstName;

    @Column(nullable = false) // Новое поле
    private String lastName;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    // В класс User.java добавь:
    @Column(name = "avatar_url")
    private String avatarUrl;

}

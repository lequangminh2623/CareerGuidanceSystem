package com.lqm.user_service.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id"),
        @NamedQuery(name = "User.findByFirstName", query = "SELECT u FROM User u WHERE u.firstName = :firstName"),
        @NamedQuery(name = "User.findByLastName", query = "SELECT u FROM User u WHERE u.lastName = :lastName"),
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM User u WHERE u.password = :password"),
        @NamedQuery(name = "User.findByAvatar", query = "SELECT u FROM User u WHERE u.avatar = :avatar"),
        @NamedQuery(name = "User.findByCreatedDate", query = "SELECT u FROM User u WHERE u.createdDate = :createdDate"),
        @NamedQuery(name = "User.findByUpdatedDate", query = "SELECT u FROM User u WHERE u.updatedDate = :updatedDate"),
        @NamedQuery(name = "User.findByGender", query = "SELECT u FROM User u WHERE u.gender = :gender"),
        @NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role")
})
public class User implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Size(min = 1, max = 255)
    @Column(name = "first_name", nullable = false)
    @ToString.Include
    private String firstName;

    @Size(min = 1, max = 255)
    @Column(name = "last_name", nullable = false)
    @ToString.Include
    private String lastName;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$")
    @Size(min = 1, max = 255)
    @Column(name = "email", nullable = false, unique = true)
    @ToString.Include
    private String email;

    @Size(min = 1, max = 255)
    @Column(name = "password", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Include
    private String password;

    @Column(name = "avatar", nullable = false)
    @ToString.Include
    private String avatar;

    @Column(name = "created_date")
    @ToString.Include
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    @ToString.Include
    private LocalDateTime updatedDate;

    @Column(name = "gender", nullable = false)
    @ToString.Include
    private Boolean gender;

    @Column(name = "role", nullable = false, length = 13)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private Role role;

    @Builder.Default
    @Column(name = "active", nullable = false)
    @ToString.Include
    private Boolean active = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Include
    private Student student;

}

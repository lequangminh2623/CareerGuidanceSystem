package com.lqm.user_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @ToString.Include
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    @ToString.Include
    private LocalDateTime updatedDate;

    @Column(name = "gender", nullable = false)
    @ToString.Include
    private Boolean gender;

    @Column(name = "role", nullable = false)
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

package com.lqm.user_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "students")
public class Student implements Serializable {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Size(min = 10, max = 10)
    @Column(name = "code", unique = true, nullable = false, length = 10)
    private String code;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

}
